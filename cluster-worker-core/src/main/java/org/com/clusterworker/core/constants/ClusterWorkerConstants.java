package org.com.clusterworker.core.constants;

import java.util.concurrent.TimeUnit;

import org.com.clusterworker.core.consumer.ConsumerStrategy;

/**
 * Constants for global definitions of ClusterWorker API
 * @author rs-renato
 * @since 1.0.0
 */
public class ClusterWorkerConstants {

	private ClusterWorkerConstants() {
	}
	
	
	/*******************************************************************************************************
	 * 											CW GENERAL CONSTANTS									   *
	 *******************************************************************************************************/
	
	/**
	 * Defines the default queue consumer strategy
	 * @since 1.0.0
	 */
	public static final ConsumerStrategy CW_QUEUE_CONSUMER_STRATEGY 	= ConsumerStrategy.ACCEPT_NULL;
	
	/**
	 * Defines the default timeout for queue consumption
	 * @since 1.0.0
	 */
	public static final int CW_QUEUE_TIMEOUT			 				= 1;
	
	/**
	 * Defines the default time unit for queue consumption timeout
	 * @since 1.0.0
	 */
	public static final TimeUnit CW_QUEUE_TIMEOUT_TIMEUNIT 				= TimeUnit.SECONDS;
	
	/**
     * Defines the default name of distributed roundrobin member name  
     * @since 1.0.0
     */
	public static final String CW_ROUND_ROBIN_MEMBER 					= "cw.roundrobin.member";
	
	/**
	 * Defines the default name of key for running producers  
	 * @since 1.0.0
	 */
	public static final String CW_RUNNING_PRODUCER 						= "cw.running.producer";
	
	
	/*******************************************************************************************************
	 * 									CW HAZELCAST CONFIGURATION CONSTANTS							   *
	 *******************************************************************************************************/
	
	/**
	 * Defines the default maximum amount of time Hazelcast will try toconnect to a well known member
	 * @since 1.0.0
	 */
	public static final int CW_NETWORK_TCP_IP_CONNECTION_TIMEOUT		= 10;
	
	/**
	 * Defines the default trusted interface
	 * @since 1.0.0
	 */
	public static final String CW_NETWORK_TRUSTED_INTERFACE				= "127.0.0.1";

	/**
	 * Defines the default distributed executor service name
	 * @since 1.0.0
	 */
	public static final String CW_EXECUTOR_SERVICE_NAME 				= "cw.executor.service";
	
	/**
	 * Defines the default number of executor threads per member for the executor.
	 * @since 1.0.0
	 */
	public static final int CW_EXECUTOR_SERVICE_MAX_POOL_SIZE			= 2;

	/**
	 * Defines the default number of executor queue capacity.
	 * @since 1.0.0
	 */
	public static final int CW_EXECUTOR_SERVICE_MAX_QUEUE_CAPACITY		= 100;

	/**
	 * Defines the default statistics enabled.
	 * @since 1.0.0
	 */
	public static final boolean CW_EXECUTOR_SERVICE_STATISTICS_ENABLED	= false;
	
	/**
	 * Defines the default rest api groups.
	 * @since 1.0.0
	 */
	public static final String[] CW_REST_API_GROUPS						= 			new String[] {"HEALTH_CHECK","CLUSTER_WRITE","CLUSTER_READ","DATA"};
	
	/**
	 * Defines the default multicast discovery mechanism
	 * @since 1.0.0
	 */
	public static final boolean CW_MULTCAST_ENABLED						= false;
	
	 /**
     * Defines the default type of logging strategy
     * @since 1.0.0
     */
    public static final String CW_LOGGING_TYPE 							= "slf4j";
    
    /**
     * Defines the default thread count for each hazelcast threads (events, io, partition operation and generic operation) 
     * @since 1.0.0
     */
	public static final String CW_THREAD_COUNT 							= "2";
    
	
	/*******************************************************************************************************
	 * 										CW QUARTZ CONSTANTS											   *
	 *******************************************************************************************************/

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
     * Defines the default quartz job's interruption on shutdown   
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
     * Defines the default quartz producer configuration detail name
     * @since 1.0.0
     */
	public static final String CW_QUARTZ_PRODUCER_CONFIG_DETAIL_NAME 	= "cw.quartz.producer.detail";
	
	/**
     * Defines the default quartz producer job detail name 
     * @since 1.0.0
     */
	public static final String CW_QUARTZ_PRODUCER_JOB_DETAIL_NAME 		= "cw.quartz.producer.job";
}