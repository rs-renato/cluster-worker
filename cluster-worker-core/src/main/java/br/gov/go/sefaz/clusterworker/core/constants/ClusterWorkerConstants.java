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
	
	// Localhost IP
	private static final String LOCALHOST_IP							= "127.0.0.1";
	
	/**
	 * Defines the default trusted interface
	 */
	public static final String CW_NETWORK_TRUSTED_INTERFACE_DEFAULT		= LOCALHOST_IP;

	/**
	 * Defines the default number of executor threads per member for the executor.
	 */
	public static final int CW_EXECUTOR_SERVICE_MAX_POOL_SIZE_DEFAULT	= 10;
	
	/**
	 * Defines the default multicast discovery mechanism
	 */
	public static final boolean CW_MULTCAST_ENABLED_DEFAULT				= false;
	
	 /**
     * Defines the type of logging strategy
     */
    public static final String CW_LOGGING_TYPE 							= "slf4j";
	
	/**
	 * Defines the default distributed executor service name
	 */
    public static final String CW_EXECUTOR_SERVICE_NAME 				= "cw.executor.service";
    
    /**
     * Defines the name of distributed roundrobin member name  
     */
	public static final String CW_ROUND_ROBIN_MEMBER 					= "cw.roundrobin.member";
	
	/**
	 * Defines the name of distributed map used to sync the next new producer instances  
	 */
	public static final String CW_PRODUCER_SYNC_EXECUTION 				= "cw.producer.sync";
	
	/**
	 * Defines the name of distributed producer last execution, used to mark the timestamp of the last production  
	 */
	public static final String CW_PRODUCER_LAST_EXECUTION 				= "cw.producer.last.execution";
}