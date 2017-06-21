package tasker;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

class ConsumingStage<In> extends Stage<In> implements ConsumingTask {
	private final Supplier<Consumer<? super In>> consumerSupplier;
	private final InstanceOptions supplierOptions;
	
	public ConsumingStage(Task<?> task, Supplier<Consumer<? super In>> consumerSupplier, InstanceOptions options) {
		super(task);
		
		this.consumerSupplier = consumerSupplier;
		this.supplierOptions = options;
	}

	public ListenableFuture<Void> execute() {
		return getTask().executeTask();
	}

	public void executeAndWait() throws InterruptedException, ExecutionException {
		ListenableFuture<Void> future = execute();
		
		future.get();
	}
	

	private Consumer<? super In> sharedConsumerInstance;
	
	@Override
	public void preExecute() {
		if (supplierOptions == InstanceOptions.Single) {
			sharedConsumerInstance = consumerSupplier.get();
		}
	}
	
	@Override
	public Runnable createExecutor(Supplier<In> supplier) {
		if (supplierOptions == InstanceOptions.Single) {
			return new StageExecutor<>(this, supplier, sharedConsumerInstance);
		} else {
			return new StageExecutor<>(this, supplier);
		}
	}
	
	@Override
	protected void postExecute(ListeningExecutorService service, int threadCount) throws ExecutionException, InterruptedException {
		// Nothing to do
	}

	
	/**
	 * Executor for consuming stages
	 * @author schmoller
	 *
	 * @param <In> The input type
	 */
	private static class StageExecutor<In> implements Runnable {
		private final ConsumingStage<In> stage;
		
		private final Supplier<In> inputSupplier;
		
		private Consumer<? super In> consumerInstance;
		
		public StageExecutor(ConsumingStage<In> stage, Supplier<In> inputSupplier) {
			Preconditions.checkNotNull(stage);
			Preconditions.checkNotNull(inputSupplier);
			Preconditions.checkState(stage.supplierOptions != InstanceOptions.Single);
			
			this.stage = stage;
			this.inputSupplier = inputSupplier;
			
			if (stage.supplierOptions == InstanceOptions.PerThread) {
				consumerInstance = stage.consumerSupplier.get();
			}
		}
		
		public StageExecutor(ConsumingStage<In> stage, Supplier<In> inputSupplier, Consumer<? super In> consumerInstance) {
			Preconditions.checkNotNull(stage);
			Preconditions.checkNotNull(inputSupplier);
			Preconditions.checkState(stage.supplierOptions == InstanceOptions.Single);
			Preconditions.checkNotNull(consumerInstance);
			
			this.stage = stage;
			this.inputSupplier = inputSupplier;
			this.consumerInstance = consumerInstance;
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
					consumerInstance = stage.consumerSupplier.get();
				}
				
				consumerInstance.accept(input);
			}
		}
	}
}
