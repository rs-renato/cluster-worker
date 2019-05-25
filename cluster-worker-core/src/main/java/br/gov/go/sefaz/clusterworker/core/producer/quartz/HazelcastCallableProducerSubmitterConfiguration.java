package br.gov.go.sefaz.clusterworker.core.producer.quartz;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;

import br.gov.go.sefaz.clusterworker.core.constants.ClusterWorkerConstants;
import br.gov.go.sefaz.clusterworker.core.producer.HazelcastCallableProducer;
import br.gov.go.sefaz.clusterworker.core.producer.callback.HazelcastMemberRoundRobinExecutionCallback;
import br.gov.go.sefaz.clusterworker.core.roundrobin.HazelcastMemberRoundRobin;

/**
 * Configuration for Quartz HazelcastCallableProducer Submitter
 * @author renato.rsilva
 * @since 1.0.0
 */
public class HazelcastCallableProducerSubmitterConfiguration{
	
	private HazelcastMemberRoundRobin hazelcastMemberRoundRobin;
	private HazelcastCallableProducer<?> hazelcastCallableProducer;
	private IExecutorService executorService;
	private HazelcastMemberRoundRobinExecutionCallback hazelcastMemberRoundRobinExecutionCallback;
	
	/**
     * Constructor for HazelcastCallableProducerSubmitterConfiguration.
     * @param configurationName configuration name to be used as roundrobin key. The roundrobin is per configuration
	 * @param hazelcastInstance hazelcast instance
	 * @param executorService executor service
	 * @param hazelcastCallableProducer callable producer
	 * @since 1.0.0
	 */
	public HazelcastCallableProducerSubmitterConfiguration(String configurationName, HazelcastInstance hazelcastInstance, IExecutorService executorService, HazelcastCallableProducer<?> hazelcastCallableProducer) {
		this.executorService = executorService;
		this.hazelcastCallableProducer = hazelcastCallableProducer;
		this.hazelcastMemberRoundRobin = new HazelcastMemberRoundRobin(hazelcastInstance, String.format("%s[%s]", ClusterWorkerConstants.CW_ROUND_ROBIN_MEMBER, configurationName));
		this.hazelcastMemberRoundRobinExecutionCallback = new HazelcastMemberRoundRobinExecutionCallback(hazelcastMemberRoundRobin);
	}
	
	/**
	 * Retrieves hazelcastMemberRoundRobin
	 * @return hazelcastMemberRoundRobin
	 * @since 1.0.0
	 */
	public HazelcastMemberRoundRobin getHazelcastMemberRoundRobin() {
		return hazelcastMemberRoundRobin;
	}


	/**
	 * Retrieves HazelcastCallableProducer
	 * @return hazelcastCallableProducer
	 * @since 1.0.0
	 */
	public HazelcastCallableProducer<?> getHazelcastCallableProducer() {
		return hazelcastCallableProducer;
	}

	/**
	 * Retrieves IExecutorService
	 * @return executorService
	 * @since 1.0.0
	 */
	public IExecutorService getExecutorService() {
		return executorService;
	}

	/**
	 * Retrieves HazelcastMemberRoundRobinExecutionCallback
	 * @return executionCallback
	 * @since 1.0.0
	 */
	public HazelcastMemberRoundRobinExecutionCallback getHazelcastMemberRoundRobinExecutionCallback() {
		return hazelcastMemberRoundRobinExecutionCallback;
	}
}