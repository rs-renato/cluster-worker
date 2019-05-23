package br.gov.go.sefaz.clusterworker.core;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
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
    private Scheduler itemProducerScheduler = initializeScheduler();
    
    private final Map<ItemProcessor<T>, HazelcastRunnableConsumer<T>> processors = new HashMap<>();
    private final Map<ItemProducer<T>, HazelcastRunnableProducer<T>> producers = new HashMap<>();

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
    	
    	// Execute this processor only if it wasn't executed before
    	if (!this.processors.containsKey(itemProcessor)) {
			
    		// Asserts mandatory exception to create an ItemProcessor
    		ConsumeFromQueue consumeFromQueue = AnnotationSupport.assertMandatoryAnnotation(itemProcessor, ConsumeFromQueue.class);
    		
    		// Number of workers (threads)
    		int workers = consumeFromQueue.workers();
    		
    		logger.info(String.format("Configuring ItemProcessor '%s' [queueName=%s, strategy=%s, timeout=%s, workers=%s]",
    				itemProcessor.getClass().getSimpleName(), consumeFromQueue.queueName(), consumeFromQueue.strategy(), consumeFromQueue.timeout(), workers));
    		
    		// Creates worker (HazelcastRunnableConsumer)
    		HazelcastRunnableConsumer<T> hazelcastRunnableConsumer = ClusterWorkerFactory.getInstance(this.hazelcastInstance).getHazelcastRunnableConsumer(itemProcessor);
    		
    		for (int i = 0; i <  workers; i++) {
    			try {
    				// Executes the consumer on hazelcast local member
    				this.executorService.executeOnMember(hazelcastRunnableConsumer, getLocalMember());
    			}catch (Exception e){
    				logger.error("Cannot execute a ItemProcessor on hazelcast executor service!", e);
    			}
    		}
    		
    		this.processors.put(itemProcessor, hazelcastRunnableConsumer);
		}
    }

    /**
     * Execute a {@link ItemProducer} client's implementation on hazelcast executor service.
     * @param itemProducer implementation of client item.
     * @since 1.0.0
     */
    public void executeItemProducer(final ItemProducer<T> itemProducer){
    	
    	// Execute this producer only if it wasn't executed before
    	if (!this.producers.containsKey(itemProducer)) {
			
    		// Asserts mandatory exception to create an ItemProducer
    		ProduceToQueue produceToQueue = AnnotationSupport.assertMandatoryAnnotation(itemProducer, ProduceToQueue.class);
    		String itemProducerName = itemProducer.getClass().getSimpleName();
    		
    		logger.info(String.format("Configuring ItemProducer '%s' [queueName=%s, maxSize=%s, cronExpression=%s]",
    				itemProducerName, produceToQueue.queueName(), produceToQueue.maxSize(), produceToQueue.cronExpression()));
    		
    		// Creates the Producer and its configuration
    		HazelcastRunnableProducer<T> hazelcastRunnableProducer = ClusterWorkerFactory.getInstance(this.hazelcastInstance).getHazelcastRunnableProducer(itemProducer);
    		
    		HazelcastRunnableProducerSubmitterConfiguration<T> produceConfig = new HazelcastRunnableProducerSubmitterConfiguration<>(itemProducerName, hazelcastInstance, executorService, hazelcastRunnableProducer);
    		
    		// Creates the trigger
    		TriggerKey triggerKey = new TriggerKey(itemProducerName, ClusterWorkerConstants.CW_QUARTZ_SCHEDULLER_NAME);
    		
    		Trigger itemProducerTrigger = TriggerBuilder.newTrigger()
    				.withIdentity(triggerKey)
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
    		
    		this.producers.put(itemProducer, hazelcastRunnableProducer);
		}
    }

    /**
     * Verifies if the thread {@link HazelcastRunnableConsumer} associated to this {@link ItemProcessor} is running.
     * @param itemProcessor item processor to verifies if is running
     * @return <code>true</code> if thread is running, <code>false</code> otherwise
     * @since 1.0.0
     */
    public boolean isRunning(ItemProcessor<T> itemProcessor) {
    	boolean isRunning = false;
    	
    	if (this.processors.containsKey(itemProcessor)) {
			isRunning = this.processors.get(itemProcessor).isRunning();
		}
    	
    	return isRunning;
    }
    
    /**
     * Verifies if the thread {@link HazelcastRunnableProducer} associated to this {@link ItemProducer} is running
     * OR if there any {@link TriggerKey} scheduled for this producer.
     * @param itemProducer item producer to verifies if is running
     * @return <code>true</code> if thread is running OR if there is any trigger scheduled, <code>false</code> otherwise
     * @since 1.0.0
     */
    public boolean isRunning(ItemProducer<T> itemProducer) {
    	boolean isRunning = false;
    	
    	if (this.producers.containsKey(itemProducer)) {
			isRunning = this.producers.get(itemProducer).isRunning();
			try {
				// Considers a job running if the thread is running or there is an schedule for this producer
				isRunning |= this.itemProducerScheduler.checkExists(new TriggerKey(itemProducer.getClass().getSimpleName(), ClusterWorkerConstants.CW_QUARTZ_SCHEDULLER_NAME));
			} catch (SchedulerException e) {
				logger.error("Could not check if quartz triggers exists. The thread running state will be returned!", e);
			}
		}
    	
    	return isRunning;
    }
    
    /**
     * Shutdown the ClusterWorker core (listeners and futures).
     * </br></br><i>Note:</i> This method DOESN'T shutdown the internal hazelcast instance and triggers!
     * @since 1.0.0
     * @see ClusterWorker#shutdown(boolean)
     */
	public void shutdown() {

		logger.warn("Shuttingdown ClusterWorker!");
		
		this.processors.values().forEach(HazelcastRunnableConsumer::shutdown);
		
		try {
			for (ItemProducer<T> itemProducer : this.producers.keySet()) {
	    		TriggerKey producerTriggerKey = new TriggerKey(itemProducer.getClass().getSimpleName(), ClusterWorkerConstants.CW_QUARTZ_SCHEDULLER_NAME);
	    		logger.warn(String.format("Unsheduling Quartz Trigger '%s'", producerTriggerKey));
	    		if (!this.itemProducerScheduler.isShutdown()) {
					this.itemProducerScheduler.unscheduleJob(producerTriggerKey);
				}
			}
			
		} catch (SchedulerException e) {
			logger.error("Could not unsheduling Quartz Triggers", e);
		}
		
		// Clears lists
		this.producers.clear();
		this.processors.clear();
		
		logger.warn("ClusterWorker shutdown completed!");
	}
	
	/**
	 * Shutdown the ClusterWorker core (listeners and futures).
	 * </br></br><i>Note:</i> If <code>shutdownHazelcast</code> was set to true, any other dependency of this hazelcast instance
	 * will be affected! Eg.: Another clusterworker instance.
	 * @param shutdownHazelcast <code>true</code> if this method should shutdown its internal hazelcast instance and triggers,
	 * <code>false</code> otherwise.
	 * @since 1.0.0
	 */
	public void shutdown(boolean shutdownHazelcast) {
		
		if (shutdownHazelcast && HazelcastSupport.isHazelcastInstanceRunning(hazelcastInstance)) {
			this.hazelcastInstance.getLifecycleService().shutdown();
			logger.warn("Hazelcast LifecycleService finished!");
			
			try {
				if (!this.itemProducerScheduler.isShutdown()) {
					this.itemProducerScheduler.shutdown();
				}
			} catch (SchedulerException e) {
				logger.error("Cannot shutdown quartz scheduler!", e);
			}
		}
		
        // Shutdown the clusterWoker core
		shutdown();
	}
	
	/**
	 * Shutdown the ItemProducer managed by this ClusterWorker
	 * @param itemProducer item producer to be shutdown
	 * @since 1.0.0
	 */
	public void shutdown(ItemProducer<T> itemProducer) {
		
		if (this.producers.containsKey(itemProducer)) {
			String itemProducerName = itemProducer.getClass().getSimpleName();
			try {
				logger.warn(String.format("Shutdown ItemProducer '%s'!", itemProducerName));
				// IF the producer is running, interrupt it
				this.itemProducerScheduler.interrupt(new JobKey(ClusterWorkerConstants.CW_QUARTZ_PRODUCER_JOB_DETAIL_NAME + itemProducerName, null));
				// Unschedule the execution of producer
				logger.warn("Unsheduling ItemProducer Quartz Trigger", itemProducerName);
				this.itemProducerScheduler.unscheduleJob(new TriggerKey(itemProducerName, ClusterWorkerConstants.CW_QUARTZ_SCHEDULLER_NAME));
				this.producers.remove(itemProducer);
			} catch (SchedulerException e) {
				logger.error(String.format("Cannot shutdown ItemProducer '%s'!", itemProducerName), e);
			}
		}
	}
	
	/**
	 * Shutdown the ItemProcessor managed by this ClusterWorker
	 * @param itemProcessor item processor to be shutdown
	 * @since 1.0.0
	 */
	public void shutdown(ItemProcessor<T> itemProcessor) {
		if (this.processors.containsKey(itemProcessor)) {
			logger.warn(String.format("Shutdown ItemProcessor '%s'!", itemProcessor.getClass().getSimpleName()));
			this.processors.get(itemProcessor).shutdown();
			this.processors.remove(itemProcessor);
		}
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
