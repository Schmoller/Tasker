package tasker;

import java.util.Collection;
import java.util.List;

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
}
