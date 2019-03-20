package br.com.clusterworker.core;

import java.util.Collection;

import org.apache.log4j.Logger;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;

import br.com.clusterworker.lock.AtomicLock;
import br.com.clusterworker.lock.LockType;

/**
 * Worker producer implementation. This class produce to {@link TaskProduce} client's implementation.
 * The role cycle of this core is controled by {@link ClusterWorker}.
 * @param <T> type of thos core producer.
 */
final class WorkerProducer<T>  extends BaseProducer<T> implements HazelcastInstanceAware, Runnable{

    private static final transient Logger logger = Logger.getLogger(WorkerProducer.class);

    private TaskAcceptable<T> taskProduce;

    public WorkerProducer(TaskProduce<T> taskProduce, String queueName) {
        super(queueName);
        this.taskProduce = taskProduce;
    }

    @Override
    public void run() {

        logger.debug("Producing on the client's implementation.");

        try{

            AtomicLock atomicLock = new AtomicLock(hazelcastInstance, getQueueName(), LockType.PRODUCER);
            TaskVisitor<T> taskVisitor = new TaskVisitorImpl<T>(atomicLock);

            Collection<T> types = taskProduce.accept(taskVisitor);

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
