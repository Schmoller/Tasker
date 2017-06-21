package tasker;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Defines an interface that chooses how to combine the results of
 * the various executions into a collection 
 * @author schmoller
 *
 * @param <T> The element type
 * @param <R> The return type
 */
public interface Collector<T,R extends Collection<? extends T>> {
	/**
	 * Combines the result lists into a single collection
	 * @param resultLists An array of lists containing the resultant elements. These lists may not have the same number of elements
	 * @return The created collection
	 */
	R collect(List<T>[] resultLists);
	
	/**
	 * Gets a collector that produces a list containing all the results
	 * @return The collector that outputs a list of T
	 */
	static <T> Collector<T,List<T>> listCollector() {
		return inputs -> {
			List<T> collected = Lists.newArrayList();
			for (List<T> input : inputs) {
				collected.addAll(input);
			}
			
			return collected;
		};
	}
	
	/**
	 * Gets a collector that produces a set containing all the results
	 * @return The collector that outputs a set of T
	 */
	static <T> Collector<T,Set<T>> setCollector() {
		return inputs -> {
			Set<T> collected = Sets.newHashSet();
			for (List<T> input : inputs) {
				collected.addAll(input);
			}
			
			return collected;
		};
	}
}
