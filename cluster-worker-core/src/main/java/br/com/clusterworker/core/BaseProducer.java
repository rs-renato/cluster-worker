package br.com.clusterworker.core;

import java.io.Serializable;
import java.util.Collection;

import org.apache.log4j.Logger;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;

import br.com.clusterworker.annotations.BaseProducerConfig;
import br.com.clusterworker.producer.Producer;
import br.com.clusterworker.utils.ClusterWorkerUtils;
import br.com.clusterworker.utils.HazelcastUtils;

/**
 * Base class for {@Producer} implementation.
 * @param <T> type of this base consumer.
 */
public class BaseProducer<T> implements Producer<T>, Serializable {

    private static final transient Logger logger = Logger.getLogger(BaseProducer.class);

    transient HazelcastInstance hazelcastInstance = HazelcastUtils.getInstance().getHazelcastInstance();

    private String queueName;

    /**
     * Creates an BaseProducer instance. This constructor requires an {@link BaseProducerConfig} annotation.
     */
    public BaseProducer() {
        BaseProducerConfig baseProducerConfig = ClusterWorkerUtils.verifyMandatoryAnotation(this, BaseProducerConfig.class);
        init(baseProducerConfig.queueName());
    }

    public BaseProducer(String queueName) {
        init(queueName);
    }

    private void init(String queueName) {
        this.queueName = queueName;
    }

    @Override
    public void produce(Collection<T> types) {

        IQueue<T> iQueue = hazelcastInstance.getQueue(queueName);

        for (T type : types) {

            try {

                logger.debug(String.format("Producing type %s to %s queue on base base producer.", type, queueName));

                iQueue.put(type);

            } catch (InterruptedException e) {
                logger.error(String.format("Cannot produce to hazelcast %s queue!", queueName), e);
            }
        }
    }

    /**
     * Return the queueName of this base producer
     *
     * @return queueName
     */
    public String getQueueName() {
        return queueName;
    }

    public void shutdown(){
        if (hazelcastInstance.getLifecycleService().isRunning()){
            hazelcastInstance.shutdown();
        }
    }
}
