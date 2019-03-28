package br.gov.go.sefaz.clusterworker.core.constants;

/**
 * Constants defined for tests
 * @author renato-rs
 * @since 1.0
 */
public class TestConstants {

	/**
	 * Hazelcast queue name
	 */
	public static final String CW_QUEUE_NAME			= "cw.queue.test";
    
	/**
	 * Quantity of items to produce to the queue
	 */
	public static final int CW_ITEM_PRODUCER_QUANTITY	= 100;
	
	/**
	 * Frequency of productioin in seconds
	 */
    public static final int CW_ITEM_PRODUCER_FREQUENCY	= 10;
    
    /**
	 * Quantity of workers (threads) of processors (queues consumers)
	 */
    public static final int CW_ITEM_PROCESSOR_WORKERS	= 5;

    /**
	 * Timeout in seconts to consume the queue
	 */
    public static final int CW_QUEUEE_TIMEOUT			= 1;
}