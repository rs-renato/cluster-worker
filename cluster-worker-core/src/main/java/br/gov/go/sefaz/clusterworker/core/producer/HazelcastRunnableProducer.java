package br.gov.go.sefaz.clusterworker.core.producer;

import java.util.Collection;

import org.apache.log4j.Logger;

import com.hazelcast.core.HazelcastInstance;

import br.gov.go.sefaz.clusterworker.core.task.TaskProducer;

/**
 * Runnable of {@link HazelcastQueueProducer}, responsible for produces {@link TaskProducer} client's implementation.
 * @author renato-rs
 * @since 1.0
 * @param <T> type which this runnable will handle.
 */
public final class HazelcastRunnableProducer<T>  extends HazelcastQueueProducer<T> implements Runnable{

	private static final transient long serialVersionUID = 2538609461091747126L;

	private static final transient Logger logger = Logger.getLogger(HazelcastRunnableProducer.class);

    private TaskProducer<T> taskProducer;

    /**
     * Constructor of HazelcastRunnableProducer
     * @param taskProducer TaskProducer client's implementation.
     * @param hazelcastInstance instance of hazelcast.
     * @param queueName queue name
     */
    public HazelcastRunnableProducer(TaskProducer<T> taskProducer, HazelcastInstance hazelcastInstance, String queueName) {
        super(hazelcastInstance, queueName);
        this.taskProducer = taskProducer;
    }

    @Override
    public void run() {

        logger.debug("Producing on the client's implementation.");

        try{
        	// Produces items from client's implementation
            Collection<T> types = taskProducer.produce();

            if (types!= null){
                produce(types);
            }

        }catch (Exception e){
            logger.error("Cannot produce on client's implementation!", e);
        }
    }
}
