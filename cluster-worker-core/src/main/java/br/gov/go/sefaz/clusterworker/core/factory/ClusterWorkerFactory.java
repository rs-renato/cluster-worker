package br.gov.go.sefaz.clusterworker.core.factory;

import br.gov.go.sefaz.clusterworker.core.ClusterWorker;
import br.gov.go.sefaz.clusterworker.core.TaskProcess;
import br.gov.go.sefaz.clusterworker.core.TaskProduce;
import br.gov.go.sefaz.clusterworker.core.annotations.TaskProcessConfig;
import br.gov.go.sefaz.clusterworker.core.annotations.TaskProduceConfig;
import br.gov.go.sefaz.clusterworker.core.consumer.WorkerConsumer;
import br.gov.go.sefaz.clusterworker.core.producer.WorkerProducer;
import br.gov.go.sefaz.clusterworker.core.utils.ClusterWorkerUtils;

/**
 * Factory for create Workers and Standalones objects.
 */
public class ClusterWorkerFactory {

    private static final ClusterWorkerFactory instance = new ClusterWorkerFactory();

    private ClusterWorkerFactory() {}

    public static ClusterWorkerFactory getInstance() {
        return instance;
    }

    /**
     * Create a new {@link ClusterWorker} instance of T type.
     * @param t type of this ClusterWorker.
     * @return ClusterWorker
     */
    public <T> ClusterWorker<T> getClusterWorker(Class<T> t){
        return new ClusterWorker<T>();
    }

    /**
     * Create a new {@link WorkerConsumer} instance of T type.
     * @param taskProcess the task process that will be executed by this WorkerConsumer.
     * @return WorkerConsumer
     */
    public <T> WorkerConsumer<T> getWorkerConsumer(TaskProcess<T> taskProcess){

        TaskProcessConfig taskProduceConfig = ClusterWorkerUtils.verifyMandatoryAnotation(taskProcess, TaskProcessConfig.class);

        return new WorkerConsumer<T>(taskProcess, taskProduceConfig.queueName(), taskProduceConfig.strategy(), taskProduceConfig.timeout());
    }

    /**
     * Create a new {@link WorkerProducer} instance of T type.
     * @param taskProduce the task process that will be executed by this WorkerProducer.
     * @return WorkerProducer
     */
    public <T> WorkerProducer<T> getWorkerProducer(TaskProduce<T> taskProduce){

        TaskProduceConfig standaloneProducerConfig = ClusterWorkerUtils.verifyMandatoryAnotation(taskProduce, TaskProduceConfig.class);

        return new WorkerProducer<T>(taskProduce, standaloneProducerConfig.queueName());
    }
}