package tasker;

import java.util.Collection;
import java.util.concurrent.ThreadFactory;
import java.util.function.Supplier;

import com.google.common.base.Preconditions;

/**
 * Provides a simple way to use task based multi-threading
 * @author schmoller
 */
public class Tasker {
	static final int UNDEFINED = -1;
	
	private int threadCount = UNDEFINED;
	private ThreadFactory threadFactory = null;
	
	/**
	 * Configures the tasker to use the given number of 
	 * threads
	 * @param count The number of threads to use
	 * @return this for chaining
	 */
	public Tasker usingThreads(int count) {
		Preconditions.checkArgument(count > 0, "Thread count cannot be less than 1");
		
		threadCount = count;
		return this;
	}
	
	/**
	 * Configures the tasker to use a one thread per
	 * available processor core.
	 * @return this for chaining
	 * @see Runtime#availableProcessors()
	 */
	public Tasker usingThreadPerCore() {
		threadCount = Runtime.getRuntime().availableProcessors();
		return this;
	}
	
	/**
	 * Configures the tasker to use a specific thread factory
	 * @param factory The thread factory
	 * @return this for chaining
	 */
	public Tasker withThreadFactory(ThreadFactory factory) {
		threadFactory = factory;
		return this;
	}
	
	/**
	 * Creates a task that consumes the given items
	 * @param items The items to consume
	 * @return The tasker
	 */
	public <T> ItemTasker<T> consume(Iterable<T> items) {
		// Use queue based approach for collections
		Supplier<T> supplier;
		if (items instanceof Collection<?>) {
			supplier = new QueueSupplier<T>((Collection<T>)items);
		} else {
			supplier = new IterableSupplier<T>(items);
		}
		
		return consume(supplier);
	}
	
	/**
	 * Creates a task that consumes the results of the supplier.
	 * The supplier MUST be thread-safe.
	 * If the supplier returns null no more items will be requested
	 * @param supplier A source for items to consume. Returning null terminates the requests
	 * @return The tasker
	 */
	public <T> ItemTasker<T> consume(Supplier<T> supplier) {
		return new Task<>(threadCount, threadFactory, supplier);
	}
}
