package br.gov.go.sefaz.clusterworker.core;

import java.util.Collection;

import org.apache.log4j.Logger;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;

/**
 * Worker producer implementation. This class produce to {@link TaskProduce} client's implementation.
 * The role cycle of this core is controled by {@link ClusterWorker}.
 * @param <T> type of thos core producer.
 */
final class WorkerProducer<T>  extends BaseProducer<T> implements HazelcastInstanceAware, Runnable{

	private static final long serialVersionUID = 2538609461091747126L;

	private static final transient Logger logger = Logger.getLogger(WorkerProducer.class);

    private TaskProduce<T> taskProduce;

    public WorkerProducer(TaskProduce<T> taskProduce, String queueName) {
        super(queueName);
        this.taskProduce = taskProduce;
    }

    @Override
    public void run() {

        logger.debug("Producing on the client's implementation.");

        try{

            Collection<T> types = taskProduce.produce();

            if (types!= null){
                produce(types);
            }

        }catch (Exception e){
            logger.error("Cannot produce on client's implementation!", e);
        }
    }

    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }
}
