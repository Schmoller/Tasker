package tasker;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface ItemTasker<T> {
	/**
	 * Consumes the results into provided consumers
	 * @param consumer The function instance to use. This MUST be threadsafe as it will
	 *                 be used by ALL threads. 
	 * @return Task settings object.
	 */
	ConsumingTask intoConsumer(Consumer<? super T> consumer);
	
	/**
	 * Consumes the results into provided consumers
	 * NOTE: This will use {@link InstanceOptions#PerThread} by default.
	 * @param consumerSupplier A supplier for consumers 
	 * @return Task settings object.
	 */
	ConsumingTask intoConsumer(Supplier<Consumer<? super T>> consumerSupplier);
	
	/**
	 * Consumes the results into provided consumers
	 * @param consumerSupplier A supplier for consumers 
	 * @param options Controls when instances of the conversion function will be acquired.
	 * @return Task settings object.
	 */
	ConsumingTask intoConsumer(Supplier<Consumer<? super T>> consumerSupplier, InstanceOptions options);
	
	/**
	 * Converts the results from one task execution to another type using a function.
	 * @param function The function instance to use. This MUST be threadsafe as it will
	 *                 be used by ALL threads. 
	 * @return Task settings object for the new type.
	 */
	<R> OutputtingTask<R> intoFunction(Function<? super T, ? extends R> function);
	
	/**
	 * Converts the results from one task execution to another type using a function.
	 * NOTE: This will use {@link InstanceOptions#PerThread} by default.
	 * @param functionSupplier A supplier for functions 
	 * @return Task settings object for the new type.
	 */
	<R> OutputtingTask<R> intoFunction(Supplier<Function<? super T, ? extends R>> functionSupplier);
	
	/**
	 * Converts the results from one task execution to another type using a function.
	 * @param functionSupplier A supplier for functions 
	 * @param options Controls when instances of the conversion function will be acquired.
	 * @return Task settings object for the new type.
	 */
	<R> OutputtingTask<R> intoFunction(Supplier<Function<? super T, ? extends R>> functionSupplier, InstanceOptions options);
}
