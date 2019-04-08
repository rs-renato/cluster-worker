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
     * Defines the type of hazelcast logging strategy
     */
    public static final String CW_HAZELCAST_LOGGING_TYPE = "slf4j";
	
	/**
	 * Defines the default distributed executor service name
	 */
    public static final String CW_EXECUTOR_SERVICE_NAME = "cw.executor.service";
    
    /**
     * Defines the name of distributed roundrobin member name  
     */
	public static final String CW_ROUND_ROBIN_MEMBER 	= "cw.roundrobin.member";
}