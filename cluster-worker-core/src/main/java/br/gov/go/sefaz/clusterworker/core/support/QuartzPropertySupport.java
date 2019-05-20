package br.gov.go.sefaz.clusterworker.core.support;

import java.util.Properties;

import br.gov.go.sefaz.clusterworker.core.constants.ClusterWorkerConstants;

/**
 * Quartz Support class
 * @author renato.rsilva
 * @since 1.0.0
 */
public class QuartzPropertySupport {

	private QuartzPropertySupport() {
	}
	
	/**
	 * Retrieves the default quartz properties
	 * @return default properties
	 * @since 1.0.0
	 */
	public static Properties getDetaultQuartzProperty() {
		Properties quartzDefaultProperties = new Properties();
		quartzDefaultProperties.setProperty("org.quartz.scheduler.instanceName", ClusterWorkerConstants.CW_QUARTZ_SCHEDULLER_NAME);
		quartzDefaultProperties.setProperty("org.quartz.scheduler.instanceId", ClusterWorkerConstants.CW_QUARTZ_SCHEDULLER_INSTANCE_NAME);
		quartzDefaultProperties.setProperty("org.quartz.threadPool.threadCount", Integer.toString(ClusterWorkerConstants.CW_QUARTZ_THREAD_POOL_COUNT));
		quartzDefaultProperties.setProperty("org.quartz.scheduler.makeSchedulerThreadDaemon", ClusterWorkerConstants.CW_QUARTZ_SCHEDULLER_AS_DAEMON);
		quartzDefaultProperties.setProperty("org.quartz.scheduler.interruptJobsOnShutdown", ClusterWorkerConstants.CW_QUARTZ_INTERRUPT_JOB_ON_SHUTDOWN);
		return quartzDefaultProperties;
	}
}
