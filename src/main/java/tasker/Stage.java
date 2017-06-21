package tasker;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

abstract class Stage<In> {
	private final Task<?> task;
	
	public Stage(Task<?> task) {
		this.task = task;
	}
	
	public Task<?> getTask() {
		return task;
	}
	
	protected abstract void preExecute();
	
	protected abstract Runnable createExecutor(Supplier<In> supplier);
	
	protected abstract void postExecute(ListeningExecutorService service, int threadCount) throws ExecutionException, InterruptedException;
	
	public void executeStage(Supplier<In> supplier, ListeningExecutorService service, int threadCount) throws ExecutionException, InterruptedException {
		List<ListenableFuture<?>> futures = Lists.newArrayList();
		
		// Prepare for execution
		preExecute();
		
		// Create and launch all threads
		for (int i = 0; i < threadCount; ++i) {
			Runnable task = createExecutor(supplier);
			futures.add(service.submit(task));
		}
		
		// Wait for all tasks to terminate
		for (ListenableFuture<?> future : futures) {
			try {
				future.get();
			} catch (InterruptedException e) {
				throw e;
			} catch (ExecutionException e) {
				throw e;
			}
		}
		
		// Process results
		postExecute(service, threadCount);
	}
}
