package tasker;

import java.util.Collection;
import java.util.concurrent.ExecutionException;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * Defines a task that produces output values
 * 
 * @author schmoller
 *
 * @param <T> The output value type
 */
public interface OutputtingTask<T> extends ItemTasker<T> {
	/**
	 * Executes the tasks according to the applied settings and
	 * gives back a ListenableFuture.
	 * @return A ListenableFuture that returns the aggregate results from all executions
	 */
	ListenableFuture<Collection<T>> execute();
	
	/**
	 * Executes the tasks according to the applied settings and
	 * gives back a ListenableFuture.
	 * @param collector A collector that defines how to package up the results
	 * @return A ListenableFuture that returns the aggregate results from all executions
	 *         according to the result of the collector
	 */
	<E extends Collection<? extends T>> ListenableFuture<E> execute(Collector<T, E> collector);
	
	/**
	 * Executes the tasks according to the applied settings and
	 * waits until it is complete.
	 * @return The aggregate results from all executions
	 */
	Collection<T> executeAndWait() throws InterruptedException, ExecutionException;
	
	/**
	 * Executes the tasks according to the applied settings and
	 * waits until it is complete.
	 * @param collector A collector that defines how to package up the results
	 * @return The aggregate results from all executions according to the result of 
	 *         the collector
	 */
	<E extends Collection<? extends T>> E executeAndWait(Collector<T, E> collector) throws InterruptedException, ExecutionException;
}
