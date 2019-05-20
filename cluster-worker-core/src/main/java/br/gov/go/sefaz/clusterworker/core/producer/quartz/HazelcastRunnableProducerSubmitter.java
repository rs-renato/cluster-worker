package br.gov.go.sefaz.clusterworker.core.producer.quartz;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.hazelcast.core.ExecutionCallback;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;

import br.gov.go.sefaz.clusterworker.core.constants.ClusterWorkerConstants;
import br.gov.go.sefaz.clusterworker.core.producer.HazelcastRunnableProducer;
import br.gov.go.sefaz.clusterworker.core.roundrobin.HazelcastMemberRoundRobin;

/**
 * Quartz HazelcastRunnableProducer Submitter 
 * @author renato-rs
 * @since 1.0.0
 */
public class HazelcastRunnableProducerSubmitter implements Job {

	private static final Logger logger = LogManager.getLogger(HazelcastRunnableProducerSubmitter.class);

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		try {
			// Retrieves the parameters
			JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
			HazelcastRunnableProducerSubmitterConfiguration<?> submitterConfig = (HazelcastRunnableProducerSubmitterConfiguration<?>) jobDataMap.get(ClusterWorkerConstants.CW_QUARTZ_PRODUCER_CONFIG_NAME);
			IExecutorService executorService = submitterConfig.getExecutorService();
			HazelcastMemberRoundRobin hazelcastMemberRoundRobin = submitterConfig.getHazelcastMemberRoundRobin();
			HazelcastRunnableProducer<?> hazelcastRunnableProducer = submitterConfig.getHazelcastRunnableProducer();
			ExecutionCallback<?> executionCallback = submitterConfig.getHazelcastMemberRoundRobinExecutionCallback();

			// Select hazelcast cluster member
			Member member = hazelcastMemberRoundRobin.select();
			boolean isLocalMember = member.localMember();

			// Executes this task only if is an local member
			if (isLocalMember) {
				logger.debug(String.format("Executing async producer task '%s'", hazelcastRunnableProducer));
				executorService.submitToMember(hazelcastRunnableProducer, member, executionCallback);
			}
			
			logger.debug(String.format("'%s' TimerTask execution %s!", hazelcastRunnableProducer, isLocalMember ? "COMPLETED" : "IGNORED"));
		} catch (Exception e) {
            logger.error("Cannot execute a ItemProducer on hazelcast executor service!", e);
		}
	}
}