package br.gov.go.sefaz.clusterworker.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;

import br.gov.go.sefaz.clusterworker.core.consumer.HazelcastRunnableConsumer;
import br.gov.go.sefaz.clusterworker.core.factory.ClusterWorkerFactory;
import br.gov.go.sefaz.clusterworker.core.producer.HazelcastRunnableProducer;
import br.gov.go.sefaz.clusterworker.core.support.AnnotationSupport;
import br.gov.go.sefaz.clusterworker.core.support.PropertySupport;
import br.gov.go.sefaz.clusterworker.core.task.TaskProcessor;
import br.gov.go.sefaz.clusterworker.core.task.TaskProducer;
import br.gov.go.sefaz.clusterworker.core.task.annotation.QueueeProcessor;
import br.gov.go.sefaz.clusterworker.core.task.annotation.QueueeProducer;

/**
 * Central core to manage hazelcast executor services and it's lifecycle.
 * @param <T> type of T
 */
public final class ClusterWorker<T> {

    private static final Logger logger = Logger.getLogger(ClusterWorker.class);

    private HazelcastInstance hazelcastInstance;
    private final IExecutorService executorService;
    private final ClusterWorkerFactory clusterWorkerFactory = ClusterWorkerFactory.getInstance();
    private Map<TaskProducer<T>, Timer> taskProducerTimerMap = new HashMap<TaskProducer<T>, Timer>();

    public ClusterWorker(HazelcastInstance hazelcastInstance) {
    	this.hazelcastInstance = hazelcastInstance;
    	this.executorService = hazelcastInstance.getExecutorService(PropertySupport.getPropertyFile("cw-config").getProperty("cw.service.task"));
    }
    
    /**
     * Execute a {@link TaskProcessor} client's implementation on hazelcast executor service.
     * @param taskProcessor implementation of client task.
     */
    public void executeTaskProccessor(TaskProcessor<T> taskProcessor){

        QueueeProcessor queueeProcessor = AnnotationSupport.assertMandatoryAnotation(taskProcessor, QueueeProcessor.class);

        int workers = queueeProcessor.workers();

        logger.info(String.format("Executing task processor (%s core(s)) implentation on hazelcast executor service.", workers));

        for (int i = 0; i <  workers; i++) {

            try {
                executorService.executeOnMember(clusterWorkerFactory.getHazelcastRunnableConsumer(taskProcessor), getLocalMember());
            }catch (Exception e){
                logger.error(String.format("Cannot execute a TaskProcessor on hazelcast executor service! %s", e.getMessage()));
            }
        }
    }

    /**
     * Execute a {@link TaskProducer} client's implementation on hazelcast executor service.
     * @param taskProducer implementation of client task.
     */
    public void executeTaskProducer(final TaskProducer<T> taskProducer){

        final HazelcastRunnableProducer<T> hazelcastRunnableProducer = clusterWorkerFactory.getHazelcastRunnableProducer(taskProducer);

        final QueueeProducer queueeProducer = AnnotationSupport.assertMandatoryAnotation(taskProducer, QueueeProducer.class);

        int frequency = queueeProducer.frequency();

        Timer timerTaskProducer = new Timer();

        logger.info(String.format("Executing task producer implentation on hazelcast executor service with frequency %s second.", frequency));

        timerTaskProducer.scheduleAtFixedRate(

                new TimerTask() {
                    @Override
                    public void run() {

                        String queueName = queueeProducer.queueName();

                        logger.info(String.format("Hazelcast queue %s size: %s", queueName, hazelcastInstance.getQueue(queueName).size()));

                        if (hazelcastInstance.getQueue(queueName).isEmpty()) {

                            boolean isLocalMember = isLocalMember();

                            if (isLocalMember) {

                                try {

                                    logger.debug("Executing task producer implementation on hazelcast executor service.");

                                    executorService.execute(hazelcastRunnableProducer);

                                } catch (Exception e) {
                                    logger.error(String.format("Cannot execute a TaskProducer on hazelcast executor service! %s", e.getMessage()));
                                }
                            }
                        }
                    }
                },
                0,
                TimeUnit.MILLISECONDS.convert(frequency, TimeUnit.SECONDS)
        );

        taskProducerTimerMap.put(taskProducer, timerTaskProducer);
    }

    /**
     * Verifies if this member is local member
     * @return true is local member
     */
    private boolean isLocalMember() {
        return hazelcastInstance.getCluster().getMembers().iterator().next().localMember();
    }

    /**
     * Returns the local cluster member
     * @return member
     */
    private Member getLocalMember() {
        return hazelcastInstance.getCluster().getLocalMember();
    }

    /**
     * Shutdown an specified timer {@link TaskProducer}
     * @param taskProducer to be canceled.
     */
    public void shutDownTask(TaskProducer<T> taskProducer){

        if (taskProducerTimerMap.containsKey(taskProducer)){
            logger.warn("Shutting down TaskProducer!");
            taskProducerTimerMap.get(taskProducer).cancel();
        }
    }

    /**
     * Shutdown entyre clusterworker core and all hazelcast instances.
     */
    public void shutDownAll(){

        logger.warn("Shutting down entire ClusterWorker!");

        for (Timer timer: taskProducerTimerMap.values()){
            timer.cancel();
            logger.warn("Timer canceled: " + timer.toString());
        }

        HazelcastRunnableConsumer.stop = true;

        if (!executorService.isShutdown()){
            executorService.shutdownNow();
            logger.warn("ExecutorService finished!");
        }

        if (hazelcastInstance.getLifecycleService().isRunning()){
            hazelcastInstance.getLifecycleService().shutdown();
            logger.warn("Hazelcast LifecycleService finished!");
        }
    }
}
