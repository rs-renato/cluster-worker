package br.gov.go.sefaz.clusterworker.core.constants;

/**
 * Constants for global definitions of ClusterWorker API
 * @author renato-rs
 * @since 1.0.0
 */
public class ClusterWorkerConstants {

	private ClusterWorkerConstants() {
	}
	
	
	/***************************************
	 * 			CW GENERAL CONSTANTS
	 ***************************************/
	
	/**
	 * Defines the default localhost IP
	 * @since 1.0.0
	 */
	private static final String LOCALHOST_IP							= "127.0.0.1";
	
	/**
	 * Defines the default maximum amount of time Hazelcast will try toconnect to a well known member
	 * @since 1.0.0
	 */
	public static int CW_NETWORK_TCP_IP_CONNECTION_TIMEOUT				= 10;
	
	/**
	 * Defines the default trusted interface
	 * @since 1.0.0
	 */
	public static final String CW_NETWORK_TRUSTED_INTERFACE_DEFAULT		= LOCALHOST_IP;

	/**
	 * Defines the default number of executor threads per member for the executor.
	 * @since 1.0.0
	 */
	public static final int CW_EXECUTOR_SERVICE_MAX_POOL_SIZE_DEFAULT	= 10;
	
	/**
	 * Defines the default multicast discovery mechanism
	 * @since 1.0.0
	 */
	public static final boolean CW_MULTCAST_ENABLED_DEFAULT				= false;
	
	 /**
     * Defines the default type of logging strategy
     * @since 1.0.0
     */
    public static final String CW_LOGGING_TYPE 							= "slf4j";
	
	/**
	 * Defines the default distributed executor service name
	 * @since 1.0.0
	 */
    public static final String CW_EXECUTOR_SERVICE_NAME 				= "cw.executor.service";
    
    /**
     * Defines the default name of distributed roundrobin member name  
     * @since 1.0.0
     */
	public static final String CW_ROUND_ROBIN_MEMBER 					= "cw.roundrobin.member";
	
	/***************************************
	 * 			CW QUARTZ CONSTANTS
	 ***************************************/

	 /**
     * Defines the defalut quartz thread pool count  
     * @since 1.0.0
     */
	public static final int CW_QUARTZ_THREAD_POOL_COUNT 				= 2;
	
	/**
     * Defines the default quartz scheduller as daemon thread  
     * @since 1.0.0
     */
	public static final String CW_QUARTZ_SCHEDULLER_AS_DAEMON			= "true";
	
	/**
     * Defines the default quartz scheduller as daemon thread  
     * @since 1.0.0
     */
	public static final String CW_QUARTZ_INTERRUPT_JOB_ON_SHUTDOWN		= "true";

	/**
     * Defines the default quartz scheduller name 
     * @since 1.0.0
     */
	public static final String CW_QUARTZ_SCHEDULLER_NAME 				= "cw.quartz.scheduler";
	
	/**
     * Defines the default quartz scheduller core instance name 
     * @since 1.0.0
     */
	public static final String CW_QUARTZ_SCHEDULLER_INSTANCE_NAME 		= "cw.quartz.scheduler.core";
	
	/**
     * Defines the default quartz producer trigger name 
     * @since 1.0.0
     */
	public static final String CW_QUARTZ_PRODUCER_TIGGER_NAME 			= "cw.quartz.producer.trigger";
	
	/**
     * Defines the default quartz producer configuration name 
     * @since 1.0.0
     */
	public static final String CW_QUARTZ_PRODUCER_CONFIG_NAME 			= "cw.quartz.producer.config";
	
	/**
     * Defines the default quartz producer job detail name 
     * @since 1.0.0
     */
	public static final String CW_QUARTZ_PRODUCER_JOB_DETAIL_NAME 		= "cw.quartz.producer.job";
}