package br.gov.go.sefaz.clusterworker.core.producer;


import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.core.HazelcastInstance;

import br.gov.go.sefaz.clusterworker.core.item.ItemProducer;

/**
 * Runnable of {@link HazelcastQueueProducer}, responsible for produces {@link ItemProducer} client's implementation.
 * @author renato-rs
 * @since 1.0
 * @param <T> type which this runnable will handle.
 */
public final class HazelcastRunnableProducer<T>  extends HazelcastQueueProducer<T> implements Runnable{

	private static final transient long serialVersionUID = 2538609461091747126L;

	private static final transient Logger logger = LogManager.getLogger(HazelcastRunnableProducer.class);

    private ItemProducer<T> itemProducer;

    /**
     * Constructor of HazelcastRunnableProducer
     * @param itemProducer ItemProducer client's implementation.
     * @param hazelcastInstance instance of hazelcast.
     * @param queueName queue name
     */
    public HazelcastRunnableProducer(ItemProducer<T> itemProducer, HazelcastInstance hazelcastInstance, String queueName) {
        super(hazelcastInstance, queueName);
        this.itemProducer = itemProducer;
    }

    @Override
    public void run() {

        logger.info("Starting HazelcastRunnableProducer!");

        try{
        	// Produces items from client's implementation
            Collection<T> items = itemProducer.produce();
            
            if (items!= null){
                produce(items);
            }

        }catch (Exception e){
            logger.error("Cannot produce on client's implementation!", e);
        }
    }
}
