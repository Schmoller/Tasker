package tasker;

import java.util.List;
import java.util.function.Supplier;

class IntermediateSupplier<Out> implements Supplier<Out> {
	private final List<Out>[] results;
	private int elementIndex;
	private int listIndex;
	
	public IntermediateSupplier(List<Out>[] results) {
		this.results = results;
		elementIndex = 0;
		listIndex = 0;
	}
	
	@Override
	public Out get() {
		synchronized (results) {
			if (listIndex >= results.length) {
				return null;
			}
			
			List<Out> list = results[listIndex];
			
			Out item = list.get(elementIndex);
			
			++elementIndex;
			if (elementIndex >= list.size()) {
				elementIndex = 0;
				++listIndex;
			}
			
			return item;
		}
	}
}
