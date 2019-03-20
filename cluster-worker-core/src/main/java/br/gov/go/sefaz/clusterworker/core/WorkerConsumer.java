package br.gov.go.sefaz.clusterworker.core;

import org.apache.log4j.Logger;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;

import br.gov.go.sefaz.clusterworker.core.utils.QueueStrategy;

/**
 * Worker consumer implementation. This class consume from {@link TaskProcess} client's implementation.
 * The role cycle of this core is controled by {@link ClusterWorker}.
 * @param <T> Type of this core consumer.
 */
final class WorkerConsumer<T> extends BaseConsumer<T> implements HazelcastInstanceAware, Runnable{

    private static final transient Logger logger = Logger.getLogger(WorkerConsumer.class);
    static boolean stop;

    private TaskAcceptable<T> taskProcess;
    private TaskVisitorImpl<T> taskVisitor;

    public WorkerConsumer(TaskProcess<T> taskProcess, String queueName, QueueStrategy queueStrategy, int timeout) {
        super(queueName, queueStrategy, timeout);
        this.taskProcess = taskProcess;
        this.taskVisitor = new TaskVisitorImpl<T>(atomicLock);
    }

    @Override
    public void run() {

        logger.info("Starting WorkerConsumer!");

        while(!stop) {

            logger.debug(String.format("Processing on the client's implementation. Strategy defined to %s.", getQueueStrategy()));

            T type = consume();

            if (type!= null){

                try{

                    taskVisitor.setType(type);

                    taskProcess.accept(taskVisitor);

                }catch (Exception e){
                    logger.error("Cannot process on client's implementation!", e);
                }
            }
        }

        logger.warn("Finishing WorkerConsumer!");
    }

    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }
}