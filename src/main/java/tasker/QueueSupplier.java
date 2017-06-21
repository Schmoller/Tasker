package tasker;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.function.Supplier;

class QueueSupplier<T> implements Supplier<T> {
	private final ArrayDeque<T> queue;
	
	public QueueSupplier(Collection<T> input) {
		queue = new ArrayDeque<T>(input);
	}
	
	public T get() {
		synchronized (queue) {
			return queue.pollFirst();
		}
	}
}
