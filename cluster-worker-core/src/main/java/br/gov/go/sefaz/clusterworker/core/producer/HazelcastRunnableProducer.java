package br.gov.go.sefaz.clusterworker.core.producer;

import java.util.Collection;

import org.apache.log4j.Logger;

import com.hazelcast.core.HazelcastInstance;

import br.gov.go.sefaz.clusterworker.core.ClusterWorker;
import br.gov.go.sefaz.clusterworker.core.task.TaskProducer;

/**
 * Worker producer implementation. This class produce to {@link TaskProducer} client's implementation.
 * The role cycle of this core is controled by {@link ClusterWorker}.
 * @param <T> type of thos core producer.
 */
public final class HazelcastRunnableProducer<T>  extends HazelcastQueueeProducer<T> implements Runnable{

	private static final long serialVersionUID = 2538609461091747126L;

	private static final transient Logger logger = Logger.getLogger(HazelcastRunnableProducer.class);

    private TaskProducer<T> taskProducer;

    public HazelcastRunnableProducer(TaskProducer<T> taskProducer, HazelcastInstance hazelcastInstance, String queueName) {
        super(hazelcastInstance, queueName);
        this.taskProducer = taskProducer;
    }

    @Override
    public void run() {

        logger.debug("Producing on the client's implementation.");

        try{

            Collection<T> types = taskProducer.produce();

            if (types!= null){
                produce(types);
            }

        }catch (Exception e){
            logger.error("Cannot produce on client's implementation!", e);
        }
    }
}
