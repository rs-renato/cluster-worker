package br.gov.go.sefaz.clusterworker.core.producer.quartz;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.hazelcast.core.ILock;
import com.hazelcast.core.Member;

import br.gov.go.sefaz.clusterworker.core.constants.ClusterWorkerConstants;

/**
 * Quartz HazelcastCallableProducer Submitter
 * @author renato.rsilva
 * @since 1.0.0
 */
@SuppressWarnings("deprecation")
public class HazelcastCallableProducerSubmitter implements Job {

	private static final Logger logger 		= LogManager.getLogger(HazelcastCallableProducerSubmitter.class);
	private static final int LOCK_TIMEOUT 	= 1;
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		ILock producerSubmitterLock = null;
		
		try {
			// Retrieves the parameters
			JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
			HazelcastCallableProducerSubmitterConfiguration submitterConfig = (HazelcastCallableProducerSubmitterConfiguration) jobDataMap.get(ClusterWorkerConstants.CW_QUARTZ_PRODUCER_CONFIG_NAME);

			// Select hazelcast cluster member
			Member member = submitterConfig.getHazelcastMemberRoundRobin().select();
			
			producerSubmitterLock = submitterConfig.getProducerSubmitterLock();
			
			boolean isLocalMember = member.localMember();

			// Retrieves the lock for produces on distributed environment
			producerSubmitterLock.lock(LOCK_TIMEOUT, TimeUnit.SECONDS);
			
			// Executes this task only if is an local member
			if (isLocalMember) {
				submitterConfig.getExecutorService()
					.submitToMember(submitterConfig.getHazelcastCallableProducer(), member, submitterConfig.getHazelcastMemberRoundRobinExecutionCallback());
			}

			logger.debug(String.format("'%s' Producer TimerTask execution %s!", context.getTrigger().getKey().getName(), isLocalMember ? "COMPLETED" : "IGNORED"));
		} catch (Exception e) {
            logger.error("Cannot execute a Producer TimerTask on hazelcast executor service!", e);
		}finally {
			if (producerSubmitterLock != null && producerSubmitterLock.isLocked()) {
				producerSubmitterLock.unlock();
			}
		}
	}
}