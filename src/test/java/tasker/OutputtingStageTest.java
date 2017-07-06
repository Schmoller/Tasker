package tasker;

import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

public class OutputtingStageTest {
	private ListeningExecutorService executorService;
	
	@Before
	public void setupExecutors() {
		// Prepare an executor service with 2 threads
		executorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(2));
	}
	
	@Test
	public void testSingleFunction() throws ExecutionException, InterruptedException {
		Function<Integer, String> testFunction = mock(Function.class);
		when(testFunction.apply(anyInt())).thenReturn("response");
		
		Task<?> task = mock(Task.class);

		Supplier<Function<Integer, String>> functionSupplier = mock(Supplier.class);
		when(functionSupplier.get()).thenReturn(testFunction);
		
		OutputtingStage<Integer, String> stage = new OutputtingStage<Integer, String>(task, functionSupplier, InstanceOptions.Single);
		
		List<Integer> testInputs = Arrays.asList(1, 2, 3, 4);
		
		// This should request only one function instance
		stage.executeStage(new IterableSupplier<>(testInputs), executorService, 2);
		
		// Only one requested
		verify(functionSupplier).get();
		
		// Called once per element
		verify(testFunction).apply(1);
		verify(testFunction).apply(2);
		verify(testFunction).apply(3);
		verify(testFunction).apply(4);
		verifyNoMoreInteractions(testFunction);
	}
	
	@Test
	public void testPerThreadFunction() throws ExecutionException, InterruptedException {
		Function<Integer, String> testFunction = mock(Function.class);
		when(testFunction.apply(anyInt())).thenReturn("response");
		
		Task<?> task = mock(Task.class);

		Supplier<Function<Integer, String>> functionSupplier = mock(Supplier.class);
		when(functionSupplier.get()).thenReturn(testFunction);
		
		OutputtingStage<Integer, String> stage = new OutputtingStage<Integer, String>(task, functionSupplier, InstanceOptions.PerThread);
		
		List<Integer> testInputs = Arrays.asList(1, 2, 3, 4);
		
		// This should request 2 function instances
		stage.executeStage(new IterableSupplier<>(testInputs), executorService, 2);
		
		// Only one requested
		verify(functionSupplier, times(2)).get();
		
		// Called once per element
		verify(testFunction).apply(1);
		verify(testFunction).apply(2);
		verify(testFunction).apply(3);
		verify(testFunction).apply(4);
		verifyNoMoreInteractions(testFunction);
	}
	
	@Test
	public void testPerTaskFunction() throws ExecutionException, InterruptedException {
		Function<Integer, String> testFunction = mock(Function.class);
		when(testFunction.apply(anyInt())).thenReturn("response");
		
		Task<?> task = mock(Task.class);

		Supplier<Function<Integer, String>> functionSupplier = mock(Supplier.class);
		when(functionSupplier.get()).thenReturn(testFunction);
		
		OutputtingStage<Integer, String> stage = new OutputtingStage<Integer, String>(task, functionSupplier, InstanceOptions.PerTask);
		
		List<Integer> testInputs = Arrays.asList(1, 2, 3, 4);
		
		// This should request 4 function instances
		stage.executeStage(new IterableSupplier<>(testInputs), executorService, 2);
		
		// Only 4 requested
		verify(functionSupplier, times(4)).get();
		
		// Called once per element
		verify(testFunction).apply(1);
		verify(testFunction).apply(2);
		verify(testFunction).apply(3);
		verify(testFunction).apply(4);
		verifyNoMoreInteractions(testFunction);
	}
}