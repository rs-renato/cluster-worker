package br.gov.go.sefaz.clusterworker.core.producer.quartz;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.hazelcast.core.Member;

import br.gov.go.sefaz.clusterworker.core.constants.ClusterWorkerConstants;

/**
 * Quartz HazelcastCallableProducer Submitter
 * @author renato.rsilva
 * @since 1.0.0
 */
public class HazelcastCallableProducerSubmitter implements Job {

	private static final Logger logger = LogManager.getLogger(HazelcastCallableProducerSubmitter.class);

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		try {
			// Retrieves the parameters
			JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
			HazelcastCallableProducerSubmitterConfiguration submitterConfig = (HazelcastCallableProducerSubmitterConfiguration) jobDataMap.get(ClusterWorkerConstants.CW_QUARTZ_PRODUCER_CONFIG_NAME);

			// Select hazelcast cluster member
			Member member = submitterConfig.getHazelcastMemberRoundRobin().select();
			boolean isLocalMember = member.localMember();

			// Executes this task only if is an local member
			if (isLocalMember) {
				submitterConfig.getExecutorService()
					.submitToMember(submitterConfig.getHazelcastCallableProducer(), member, submitterConfig.getHazelcastMemberRoundRobinExecutionCallback());
			}

			logger.debug(String.format("'%s' Producer TimerTask execution %s!", context.getTrigger().getKey().getName(), isLocalMember ? "COMPLETED" : "IGNORED"));
		} catch (Exception e) {
            logger.error("Cannot execute a ItemProducer on hazelcast executor service!", e);
		}
	}
}