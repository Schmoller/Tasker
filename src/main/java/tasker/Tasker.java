package tasker;

import java.util.concurrent.ThreadFactory;
import java.util.function.Supplier;

/**
 * Provides a simple way to use task based multi-threading
 * @author schmoller
 */
public class Tasker {
	/**
	 * Configures the tasker to use the given number of 
	 * threads
	 * @param count The number of threads to use
	 * @return this for chaining
	 */
	public Tasker usingThreads(int count) {
		return this;
	}
	
	/**
	 * Configures the tasker to use a one thread per
	 * available processor core.
	 * @return this for chaining
	 * @see Runtime#availableProcessors()
	 */
	public Tasker usingThreadPerCore() {
		return this;
	}
	
	/**
	 * Configures the tasker to use a specific thread factory
	 * @param factory The thread factory
	 * @return this for chaining
	 */
	public Tasker withThreadFactory(ThreadFactory factory) {
		return this;
	}
	
	/**
	 * Creates a task that consumes the given items
	 * @param items The items to consume
	 * @return The tasker
	 */
	public <T> ItemTasker<T> consume(Iterable<T> items) {
		
	}
	
	/**
	 * Creates a task that consumes the results of the supplier.
	 * The supplier MUST be thread-safe.
	 * If the supplier returns null no more items will be requested
	 * @param supplier A source for items to consume. Returning null terminates the requests
	 * @return The tasker
	 */
	public <T> ItemTasker<T> consume(Supplier<T> supplier) {
		
	}
}
