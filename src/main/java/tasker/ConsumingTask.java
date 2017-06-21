package tasker;

import java.util.concurrent.ExecutionException;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * Defines a task that consumes values
 * 
 * @author schmoller
 */
public interface ConsumingTask {
	/**
	 * Executes the tasks according to the applied settings and
	 * gives back a ListenableFuture.
	 * @return A ListenableFuture that can be used to check when execution is finished
	 */
	ListenableFuture<Void> execute();
	
	/**
	 * Executes the tasks according to the applied settings and
	 * waits until it is complete.
	 */
	void executeAndWait() throws InterruptedException, ExecutionException;
}
