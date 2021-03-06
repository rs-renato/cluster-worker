package org.com.clusterworker.core.support;

/**
 * Constants defined for tests
 * @author rs-renato
 * @since 1.0.0
 */
public class TestConstants {

	/**
	 * Cluster Woker Name (used to hazelcast instance)
	 */
	public static final String CW_NAME							= "cw";
    
	/**
	 * Hazelcast integer queue name
	 */
	public static final String CW_INTEGER_QUEUE_NAME			= "cw.queue.integer";
	
	/**
	 * Hazelcast string queue name
	 */
	public static final String CW_STRING_QUEUE_NAME				= "cw.queue.string";
	
	/**
	 * Quantity of items to produce to the queue
	 */
	public static final int CW_ITEM_PRODUCER_QUANTITY			= 10;
	
	
	/**
	 * Max quantity of items to produce to the queue
	 */
	public static final int CW_ITEM_PRODUCER_MAX_QUANTITY		= CW_ITEM_PRODUCER_QUANTITY;
	
	/**
	 * Cron expression to define the frequency of production
	 */
    public static final String CW_ITEM_PRODUCER_CRON_EXPRESSION	= "0/5 * * * * ?";
    
    /**
	 * Quantity of workers (threads) of processors (queues consumers)
	 */
    public static final int CW_ITEM_PROCESSOR_WORKERS			= 2;

    /**
	 * Timeout in seconts to consume the queue
	 */
    public static final int CW_QUEUEE_TIMEOUT					= 1;

    /**
     * Time to execute a test (in seconds)
     */
	public static final long CW_EXECUTION_TIME 					= 15;
}