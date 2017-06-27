package tasker;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

class Task<In> implements ItemTasker<In> {
	private final Supplier<In> supplier;
	private Stage<In> firstStage;
	
	private final int maxThreads;
	private final ListeningExecutorService executorService;
	
	public Task(int threadCount, ThreadFactory threadFactory, Supplier<In> supplier) {
		this.supplier = supplier;
		this.maxThreads = threadCount;
		
		ExecutorService baseService;
		if (threadFactory == null) {
			baseService = Executors.newCachedThreadPool();
		} else {
			baseService = Executors.newCachedThreadPool(threadFactory);
		}
		
		executorService = MoreExecutors.listeningDecorator(baseService);
	}
	
	public ConsumingTask intoConsumer(Consumer<? super In> consumer) {
		return intoConsumer(() -> consumer, InstanceOptions.Single);
	}

	public ConsumingTask intoConsumer(Supplier<? extends Consumer<? super In>> consumerSupplier) {
		return intoConsumer(consumerSupplier, InstanceOptions.PerThread);
	}

	public ConsumingTask intoConsumer(Supplier<? extends Consumer<? super In>> consumerSupplier, InstanceOptions options) {
		ConsumingStage<In> next = new ConsumingStage<>(this, consumerSupplier, options);
		firstStage = next;
		return next;
	}

	public <R> OutputtingTask<R> intoFunction(Function<? super In, ? extends R> function) {
		return intoFunction(() -> function, InstanceOptions.Single);
	}

	public <R> OutputtingTask<R> intoFunction(Supplier<? extends Function<? super In, ? extends R>> functionSupplier) {
		return intoFunction(functionSupplier, InstanceOptions.PerThread);
	}

	public <R> OutputtingTask<R> intoFunction(Supplier<? extends Function<? super In, ? extends R>> functionSupplier, InstanceOptions options) {
		OutputtingStage<In, R> next = new OutputtingStage<>(this, functionSupplier, options);
		firstStage = next;
		return next;
	}
	
	/**
	 * Executes the entire task
	 * @return A future that will complete once the task execution is finished
	 */
	public ListenableFuture<Void> executeTask() {
		TaskController controller = new TaskController();
		executorService.submit(controller);
		return controller;
	}
	
	private class TaskController extends AbstractFuture<Void> implements Runnable {

		@Override
		public void run() {
			try {
				firstStage.executeStage(supplier, executorService, maxThreads);
				executorService.shutdown();
				set(null);
			} catch (ExecutionException e) {
				// Pass the real cause in, no need for the intermediary
				setException(e.getCause());
			} catch (InterruptedException e) {
				// Interrupt this thread too
				Thread.currentThread().interrupt();
			} catch (Throwable e) {
				setException(e);
			}
		}
	}
}
