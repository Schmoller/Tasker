package tasker;

/**
 * Controls the behaviour of suppliers for providing instances.
 * @author schmoller
 */
public enum InstanceOptions {
	/**
	 * Only one single instance will be provided.
	 * The instance MUST be thread-safe as it will
	 * be used by ALL threads. 
	 */
	Single,
	/**
	 * One instance will be provided per thread.
	 * The instance does NOT need to be thread-safe.
	 */
	PerThread,
	/**
	 * One instance will be provided per task.
	 * The instance does NOT need to be thread-safe.
	 */
	PerTask
}
