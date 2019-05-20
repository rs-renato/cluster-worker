package br.gov.go.sefaz.clusterworker.core;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;

import br.gov.go.sefaz.clusterworker.core.annotation.ConsumeFromQueue;
import br.gov.go.sefaz.clusterworker.core.annotation.ProduceToQueue;
import br.gov.go.sefaz.clusterworker.core.constants.ClusterWorkerConstants;
import br.gov.go.sefaz.clusterworker.core.consumer.HazelcastRunnableConsumer;
import br.gov.go.sefaz.clusterworker.core.factory.ClusterWorkerFactory;
import br.gov.go.sefaz.clusterworker.core.item.ItemProcessor;
import br.gov.go.sefaz.clusterworker.core.item.ItemProducer;
import br.gov.go.sefaz.clusterworker.core.listener.ShutdownListener;
import br.gov.go.sefaz.clusterworker.core.producer.HazelcastRunnableProducer;
import br.gov.go.sefaz.clusterworker.core.producer.quartz.HazelcastRunnableProducerSubmitter;
import br.gov.go.sefaz.clusterworker.core.producer.quartz.HazelcastRunnableProducerSubmitterConfiguration;
import br.gov.go.sefaz.clusterworker.core.support.AnnotationSupport;
import br.gov.go.sefaz.clusterworker.core.support.HazelcastSupport;
import br.gov.go.sefaz.clusterworker.core.support.QuartzPropertySupport;
/**
 * Central core to manage hazelcast executor services and it's lifecycle.
 * @author renato.rsilva
 * @since 1.0.0
 * @param <T> type which ClusterWorker will handle.
 */
public final class ClusterWorker<T> {

	private static final Logger logger = LogManager.getLogger(ClusterWorker.class);

    private final HazelcastInstance hazelcastInstance;
    private final IExecutorService executorService;
    private final ArrayList<ShutdownListener> shutdownListeners = new ArrayList<>();
    private Scheduler itemProducerScheduler = initializeScheduler();
    
    /**
     * Constructor for ClusterWorker.
     * @param hazelcastInstance instance of hazelcast.
     * @since 1.0.0
     */
    public ClusterWorker(HazelcastInstance hazelcastInstance) {
    	this.hazelcastInstance = hazelcastInstance;
        this.executorService = hazelcastInstance.getExecutorService(ClusterWorkerConstants.CW_EXECUTOR_SERVICE_NAME);
    }
    
    /**
     * Execute a {@link ItemProcessor} client's implementation on hazelcast executor service.
     * @param itemProcessor implementation of client item.
     * @since 1.0.0
     */
    public void executeItemProccessor(ItemProcessor<T> itemProcessor){
    	// Asserts mandatory exception to create an ItemProcessor
        ConsumeFromQueue consumeFromQueue = AnnotationSupport.assertMandatoryAnnotation(itemProcessor, ConsumeFromQueue.class);

        // Number of workers (threads)
        int workers = consumeFromQueue.workers();

        logger.info(String.format("Configuring ItemProcessor (%s worker(s)) implementation on hazelcast executor service.", workers));

        // Creates worker (HazelcastRunnableConsumer)
        HazelcastRunnableConsumer<T> hazelcastRunnableConsumer = ClusterWorkerFactory.getInstance(this.hazelcastInstance).getHazelcastRunnableConsumer(itemProcessor);
        this.shutdownListeners.add(hazelcastRunnableConsumer);
        
        for (int i = 0; i <  workers; i++) {
            try {
            	// Executes the consumer on hazelcast local member
				this.executorService.executeOnMember(hazelcastRunnableConsumer, getLocalMember());
            }catch (Exception e){
                logger.error("Cannot execute a ItemProcessor on hazelcast executor service!", e);
            }
        }
    }

    /**
     * Execute a {@link ItemProducer} client's implementation on hazelcast executor service.
     * @param itemProducer implementation of client item.
     * @since 1.0.0
     */
    public void executeItemProducer(final ItemProducer<T> itemProducer){
    	// Asserts mandatory exception to create an ItemProducer
    	ProduceToQueue produceToQueue = AnnotationSupport.assertMandatoryAnnotation(itemProducer, ProduceToQueue.class);
    	String itemProducerName = itemProducer.getClass().getSimpleName();
    	
    	// Creates the Producer and its configuration
		HazelcastRunnableProducer<T> hazelcastRunnableProducer = ClusterWorkerFactory.getInstance(this.hazelcastInstance).getHazelcastRunnableProducer(itemProducer);
		HazelcastRunnableProducerSubmitterConfiguration<T> produceConfig = new HazelcastRunnableProducerSubmitterConfiguration<>(itemProducerName, hazelcastInstance, executorService, hazelcastRunnableProducer);

		// Creates the trigger
    	Trigger itemProducerTrigger = TriggerBuilder.newTrigger()
    				.withIdentity(itemProducerName, ClusterWorkerConstants.CW_QUARTZ_SCHEDULLER_NAME)
    				.withSchedule(CronScheduleBuilder.cronSchedule(produceToQueue.cronExpression()))
    				.build();

    	// Creates the job map
    	JobDataMap jobData = new JobDataMap();
		jobData.put(ClusterWorkerConstants.CW_QUARTZ_PRODUCER_CONFIG_NAME, produceConfig);
    	
		// Creates the submitter
		JobDetail itemProducerSubmitter = JobBuilder.newJob(HazelcastRunnableProducerSubmitter.class)
    			.withIdentity(ClusterWorkerConstants.CW_QUARTZ_PRODUCER_JOB_DETAIL_NAME + itemProducerName)
    			.usingJobData(jobData)
    			.build();
    	
		try {
			// Schedule the item producer execution
			itemProducerScheduler.scheduleJob(itemProducerSubmitter, itemProducerTrigger);
		} catch (SchedulerException e) {
            logger.error("Cannot schedule a ItemProcessor on quartz!", e);
		}
    }

    /**
     * Shutdown the ClusterWorker core (listeners and futures).
     * </br></br><i>Note:</i> This method DOESN'T shutdown the internal hazelcast instance!
     * @since 1.0.0
     * @see ClusterWorker#shutdown(boolean)
     */
	public void shutdown() {

		logger.warn("Shuttingdown ClusterWorker!");
		
		logger.warn("Shuttingdown Listeners ..");
		this.shutdownListeners.forEach(ShutdownListener::shutdown);
		
		try {
			itemProducerScheduler.shutdown();
		} catch (SchedulerException e) {
			logger.error("Cannot shutdown quartz scheduler!", e);
		}
		// Clears lists
		this.shutdownListeners.clear();
		
		logger.warn("ClusterWorker shutdown completed!");
	}
	
	/**
	 * Shutdown the ClusterWorker core (listeners and futures).
	 * </br></br><i>Note:</i> If <code>shutdownHazelcast</code> was set to true, any other dependency of this hazelcast instance
	 * will be affected! Eg.: Another clusterworker instance.
	 * @param shutdownHazelcast <code>true</code> if this method should shutdown its internal hazelcast instance,
	 * <code>false</code> otherwise.
	 * @since 1.0.0
	 */
	public void shutdown(boolean shutdownHazelcast) {
		
		if (shutdownHazelcast && HazelcastSupport.isHazelcastInstanceRunning(hazelcastInstance)) {
			this.hazelcastInstance.getLifecycleService().shutdown();
			logger.warn("Hazelcast LifecycleService finished!");
		}
        // Shutdown the clusterWoker core
		shutdown();
	}
	
	/**
	 * Initialize quartz scheduler
	 * @return Scheduler
	 * @since 1.0.0
	 */
	private Scheduler initializeScheduler() {
		try {
    		itemProducerScheduler = new StdSchedulerFactory(QuartzPropertySupport.getDetaultQuartzProperty()).getScheduler();
    		itemProducerScheduler.start();
		} catch (Exception e) {
            logger.error("Cannot create quartz scheduler!", e);
		}
		
		return itemProducerScheduler;
	}
    
    /**
     * Returns the local cluster member.
     * @return member
     * @since 1.0.0
     */
    private Member getLocalMember() {
        return this.hazelcastInstance.getCluster().getLocalMember();
    }
}
