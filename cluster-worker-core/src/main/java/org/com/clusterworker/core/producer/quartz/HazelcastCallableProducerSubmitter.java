package org.com.clusterworker.core.producer.quartz;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.com.clusterworker.core.constants.ClusterWorkerConstants;
import org.com.clusterworker.core.producer.HazelcastCallableProducer;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.hazelcast.core.ILock;
import com.hazelcast.core.Member;

/**
 * Quartz HazelcastCallableProducer Submitter
 * @author rs-renato
 * @since 1.0.0
 */
@SuppressWarnings("deprecation")
public class HazelcastCallableProducerSubmitter implements Job {

	private static final Logger logger 				= LogManager.getLogger(HazelcastCallableProducerSubmitter.class);
	private static final int LOCK_TIMEOUT 			= 1;
	private static final String LOG_MESSAGE_PATTERN	="'%s' Producer TimerTask execution %s!";
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		ILock producerSubmitterLock = null;
		
		try {
			// Retrieves the parameters
			JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
			HazelcastCallableProducerSubmitterDetail produceSubmitterDetail = (HazelcastCallableProducerSubmitterDetail) jobDataMap.get(ClusterWorkerConstants.CW_QUARTZ_PRODUCER_CONFIG_DETAIL_NAME);
			HazelcastCallableProducer<?> hazelcastCallableProducer = produceSubmitterDetail.getHazelcastCallableProducer();
				
			// Select hazelcast cluster member
			Member member = produceSubmitterDetail.getHazelcastMemberRoundRobin().select();
			
			producerSubmitterLock = produceSubmitterDetail.getProducerSubmitterLock();
			
			boolean isLocalMember = member.localMember();
			String triggerKey = context.getTrigger().getKey().getName();
			
			// Retrieves the lock for produces on distributed environment
			producerSubmitterLock.lock(LOCK_TIMEOUT, TimeUnit.SECONDS);
			
			// Executes this task only if is an local member
			if (isLocalMember) {
				// Executes the producer only if is not running
				if (!hazelcastCallableProducer.isRunning()) {
					produceSubmitterDetail.getExecutorService()
						.submitToMember(hazelcastCallableProducer, member, produceSubmitterDetail.getHazelcastMemberRoundRobinExecutionCallback());
					
					logger.info(String.format(LOG_MESSAGE_PATTERN, triggerKey, "COMPLETED (SELECTED LOCAL MEMBER)"));

				}else {
					logger.warn(String.format(LOG_MESSAGE_PATTERN, triggerKey, "IGNORED (PRODUCER ALREADY RUNNING)"));
				}
			}else {
				logger.warn(String.format(LOG_MESSAGE_PATTERN, triggerKey, "IGNORED (SELECTED REMOTE MEMBER)"));
			}
			
		} catch (Exception e) {
            logger.error("Cannot execute a Producer TimerTask on hazelcast executor service!", e);
		}finally {
			if (producerSubmitterLock != null && producerSubmitterLock.isLocked()) {
				producerSubmitterLock.unlock();
			}
		}
	}
}