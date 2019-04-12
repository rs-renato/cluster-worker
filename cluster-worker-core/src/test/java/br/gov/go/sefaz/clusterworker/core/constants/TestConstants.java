package br.gov.go.sefaz.clusterworker.core.constants;

/**
 * Constants defined for tests
 * @author renato-rs
 * @since 1.0
 */
public class TestConstants {

	/**
	 * Cluster Woker Name (used to hazelcast instance)
	 */
	public static final String CW_NAME							= "cw";
    
	/**
	 * Hazelcast queue name
	 */
	public static final String CW_QUEUE_NAME					= "cw.queue.test";
	
	/**
	 * Quantity of items to produce to the queue
	 */
	public static final int CW_ITEM_PRODUCER_QUANTITY			= 10;
	
	
	/**
	 * Max quantity of items to produce to the queue
	 */
	public static final int CW_ITEM_PRODUCER_MAX_QUANTITY		= CW_ITEM_PRODUCER_QUANTITY;
	
	/**
	 * Frequency of productioin in seconds
	 */
    public static final int CW_ITEM_PRODUCER_FREQUENCY			= 30;
    
    /**
	 * Quantity of workers (threads) of processors (queues consumers)
	 */
    public static final int CW_ITEM_PROCESSOR_WORKERS			= 2;

    /**
	 * Timeout in seconts to consume the queue
	 */
    public static final int CW_QUEUEE_TIMEOUT					= 1;

    /**
     * Time to execute a test
     */
	public static final long CW_EXECUTION_TIME 					= 5 * 60 * 1000;
}