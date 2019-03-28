package br.gov.go.sefaz.clusterworker.core.constants;

/**
 * Constants for global definitions of ClusterWorker API
 * @author renato-rs
 * @since 1.0
 *
 */
public class ClusterWorkerConstants {

	private ClusterWorkerConstants() {
	}
	
	
	/**
	 * Defines the default Hazelcast Instance Name
	 */
    public static final String CW_HAZELCAST_INSTANCE_NAME = "ClusterWorker";
    
	/**
	 * Defines the default executor service name
	 */
    public static final String CW_EXECUTOR_SERVICE_NAME = "cw.executor.service";
    
    /**
     * Defines the default queue config name
     */
    public static final String CW_QUEUE_CONFIG_DEFAULT = "cw.queue.config.default";
}
