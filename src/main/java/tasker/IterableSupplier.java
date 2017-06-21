package tasker;

import java.util.Iterator;
import java.util.function.Supplier;

class IterableSupplier<T> implements Supplier<T> {
	private final Iterator<T> source;
	
	public IterableSupplier(Iterable<T> input) {
		source = input.iterator();
	}
	
	public T get() {
		synchronized (source) {
			if (source.hasNext()) {
				return source.next();
			} else {
				return null;
			}
		}
	}
}
