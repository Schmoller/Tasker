package tasker;

import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

public class ConsumingStageTest {
	private ListeningExecutorService executorService;
	
	@Before
	public void setupExecutors() {
		// Prepare an executor service with 2 threads
		executorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(2));
	}
	
	@Test
	public void testSingleConsumer() throws ExecutionException, InterruptedException {
		Consumer<String> testConsumer = mock(Consumer.class);
		Task<?> task = mock(Task.class);

		Supplier<Consumer<String>> consumerSupplier = mock(Supplier.class);
		when(consumerSupplier.get()).thenReturn(testConsumer);
		
		ConsumingStage<String> stage = new ConsumingStage<String>(task, consumerSupplier, InstanceOptions.Single);
		
		List<String> testInputs = Arrays.asList("a", "b", "c", "d");
		
		// This should request only one consumer instance
		stage.executeStage(new IterableSupplier<>(testInputs), executorService, 2);
		
		// Only one requested
		verify(consumerSupplier).get();
		
		// Called once per element
		verify(testConsumer).accept("a");
		verify(testConsumer).accept("b");
		verify(testConsumer).accept("c");
		verify(testConsumer).accept("d");
		verifyNoMoreInteractions(testConsumer);
	}
	
	@Test
	public void testPerThreadConsumer() throws ExecutionException, InterruptedException {
		Consumer<String> testConsumer = mock(Consumer.class);
		Task<?> task = mock(Task.class);

		Supplier<Consumer<String>> consumerSupplier = mock(Supplier.class);
		when(consumerSupplier.get()).thenReturn(testConsumer);
		
		ConsumingStage<String> stage = new ConsumingStage<String>(task, consumerSupplier, InstanceOptions.PerThread);
		
		List<String> testInputs = Arrays.asList("a", "b", "c", "d");
		
		// This should request only one per thread (2)
		stage.executeStage(new IterableSupplier<>(testInputs), executorService, 2);
		
		// Only 2 requested
		verify(consumerSupplier, times(2)).get();
		
		// Called once per element
		verify(testConsumer).accept("a");
		verify(testConsumer).accept("b");
		verify(testConsumer).accept("c");
		verify(testConsumer).accept("d");
		verifyNoMoreInteractions(testConsumer);
	}
	
	@Test
	public void testPerTaskConsumer() throws ExecutionException, InterruptedException {
		Consumer<String> testConsumer = mock(Consumer.class);
		Task<?> task = mock(Task.class);

		Supplier<Consumer<String>> consumerSupplier = mock(Supplier.class);
		when(consumerSupplier.get()).thenReturn(testConsumer);
		
		ConsumingStage<String> stage = new ConsumingStage<String>(task, consumerSupplier, InstanceOptions.PerTask);
		
		List<String> testInputs = Arrays.asList("a", "b", "c", "d");
		
		// This should request one per item (4)
		stage.executeStage(new IterableSupplier<>(testInputs), executorService, 2);
		
		// Only 4 requested
		verify(consumerSupplier, times(4)).get();
		
		// Called once per element
		verify(testConsumer).accept("a");
		verify(testConsumer).accept("b");
		verify(testConsumer).accept("c");
		verify(testConsumer).accept("d");
		verifyNoMoreInteractions(testConsumer);
	}

}
