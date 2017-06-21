package tasker;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

class OutputtingStage<In,Out> extends Stage<In> implements OutputtingTask<Out> {
	private Supplier<Function<? super In, ? extends Out>> functionSupplier;
	private InstanceOptions supplierOptions;
	
	private Stage<Out> nextStage;
	
	public OutputtingStage(Task<?> task, Supplier<Function<? super In, ? extends Out>> functionSupplier, InstanceOptions options) {
		super(task);
		this.functionSupplier = functionSupplier;
		this.supplierOptions = options;
	}

	public ConsumingTask intoConsumer(Consumer<? super Out> consumer) {
		return intoConsumer(() -> consumer, InstanceOptions.Single);
	}

	public ConsumingTask intoConsumer(Supplier<Consumer<? super Out>> consumerSupplier) {
		return intoConsumer(consumerSupplier, InstanceOptions.PerThread);
	}

	public ConsumingTask intoConsumer(Supplier<Consumer<? super Out>> consumerSupplier, InstanceOptions options) {
		ConsumingStage<Out> next = new ConsumingStage<>(getTask(), consumerSupplier, options);
		nextStage = next;
		return next;
	}

	public <R> OutputtingTask<R> intoFunction(Function<? super Out, ? extends R> function) {
		return intoFunction(() -> function, InstanceOptions.Single);
	}

	public <R> OutputtingTask<R> intoFunction(Supplier<Function<? super Out, ? extends R>> functionSupplier) {
		return intoFunction(functionSupplier, InstanceOptions.PerThread);
	}

	public <R> OutputtingTask<R> intoFunction(Supplier<Function<? super Out, ? extends R>> functionSupplier, InstanceOptions options) {
		OutputtingStage<Out, R> next = new OutputtingStage<>(getTask(), functionSupplier, options);
		nextStage = next;
		return next;
	}

	public ListenableFuture<? extends Collection<Out>> execute() {
		return execute(Collector.listCollector());
	}

	public <E extends Collection<? extends Out>> ListenableFuture<E> execute(Collector<Out, E> collector) {
		Preconditions.checkState(nextStage == null, "A follow-up task has been supplied, this cannot be executed anymore");
		ListenableFuture<Void> future = getTask().executeTask();
		
		OutputFuture<E> outputFuture = new OutputFuture<>(collector);
		Futures.addCallback(future, outputFuture);
		
		return outputFuture;
	}

	public Collection<Out> executeAndWait() throws InterruptedException, ExecutionException {
		return executeAndWait(Collector.listCollector());
	}

	public <E extends Collection<? extends Out>> E executeAndWait(Collector<Out, E> collector) throws InterruptedException, ExecutionException {
		ListenableFuture<E> future = execute(collector);
		
		return future.get();
	}
	
	
	private Function<? super In, ? extends Out> sharedFunctionInstance;
	private List<StageExecutor<In, Out>> createdExecutors;
	
	@Override
	protected void preExecute() {
		if (supplierOptions == InstanceOptions.Single) {
			sharedFunctionInstance = functionSupplier.get();
		}
		
		createdExecutors = Lists.newArrayList();
	}
	
	@Override
	protected Runnable createExecutor(Supplier<In> supplier) {
		StageExecutor<In,Out> executor;
		if (supplierOptions == InstanceOptions.Single) {
			executor = new StageExecutor<>(this, supplier, sharedFunctionInstance);
		} else {
			executor = new StageExecutor<>(this, supplier);
		}
		
		createdExecutors.add(executor);
		return executor;
	}
	
	@SuppressWarnings("unchecked")
	private List<Out>[] retrieveResults() {
		List<Out>[] results = new List[createdExecutors.size()];
		for (int i = 0; i < createdExecutors.size(); ++i) {
			results[i] = createdExecutors.get(i).outputCache;
		}
		
		return results;
	}
	
	@Override
	protected void postExecute(ListeningExecutorService service, int threadCount) throws ExecutionException, InterruptedException {
		if (nextStage != null) {
			Supplier<Out> nextSupplier = new IntermediateSupplier<>(retrieveResults());
			
			nextStage.executeStage(nextSupplier, service, threadCount);
		}
	}
	

	/**
	 * Executor for outputting stages
	 * @author schmoller
	 *
	 * @param <In> The input type
	 * @param <Out> The output type
	 */
	private static class StageExecutor<In, Out> implements Runnable {
		private final OutputtingStage<In, Out> stage;
		
		private final Supplier<In> inputSupplier;
		private final List<Out> outputCache;
		
		private Function<? super In, ? extends Out> functionInstance;
		
		public StageExecutor(OutputtingStage<In, Out> stage, Supplier<In> inputSupplier) {
			Preconditions.checkNotNull(stage);
			Preconditions.checkNotNull(inputSupplier);
			Preconditions.checkState(stage.supplierOptions != InstanceOptions.Single);
			
			this.stage = stage;
			this.inputSupplier = inputSupplier;
			
			outputCache = Lists.newArrayList();
			
			if (stage.supplierOptions == InstanceOptions.PerThread) {
				functionInstance = stage.functionSupplier.get();
			}
		}
		
		public StageExecutor(OutputtingStage<In, Out> stage, Supplier<In> inputSupplier, Function<? super In, ? extends Out> functionInstance) {
			Preconditions.checkNotNull(stage);
			Preconditions.checkNotNull(inputSupplier);
			Preconditions.checkState(stage.supplierOptions == InstanceOptions.Single);
			Preconditions.checkNotNull(functionInstance);
			
			this.stage = stage;
			this.inputSupplier = inputSupplier;
			this.functionInstance = functionInstance;
			
			outputCache = Lists.newArrayList();
		}
		
		@Override
		public void run() {
			while (true) {
				// Next object to process
				In input = inputSupplier.get();
				if (input == null) {
					return;
				}
				
				// Retrieve the per task instance if needed
				if (stage.supplierOptions == InstanceOptions.PerTask) {
					functionInstance = stage.functionSupplier.get();
				}
				
				Out result = functionInstance.apply(input);
				outputCache.add(result);
			}
		}
	}
	
	private class OutputFuture<E extends Collection<? extends Out>> extends AbstractFuture<E> implements FutureCallback<Void> {
		private final Collector<Out, E> collector;
		
		public OutputFuture(Collector<Out, E> collector) {
			this.collector = collector;
		}
		
		@Override
		public void onSuccess(Void result) {
			List<Out>[] results = retrieveResults();
			
			try {
				set(collector.collect(results));
			} catch (Throwable e) {
				setException(e);
			}
		}

		@Override
		public void onFailure(Throwable t) {
			if (t instanceof ExecutionException) {
				setException(t.getCause());
			} else if (t instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			} else {
				setException(t);
			}
		}
		
	}
}
