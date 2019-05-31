package br.gov.go.sefaz.clusterworker.core.producer.quartz;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.ILock;

import br.gov.go.sefaz.clusterworker.core.constants.ClusterWorkerConstants;
import br.gov.go.sefaz.clusterworker.core.producer.HazelcastCallableProducer;
import br.gov.go.sefaz.clusterworker.core.producer.callback.HazelcastMemberRoundRobinExecutionCallback;
import br.gov.go.sefaz.clusterworker.core.roundrobin.HazelcastMemberRoundRobin;

/**
 * Configuration Details for Quartz HazelcastCallableProducer Submitter
 * @author renato.rsilva
 * @since 1.0.0
 */
@SuppressWarnings("deprecation")
public class HazelcastCallableProducerSubmitterDetail{
	
	private IExecutorService executorService;
	private ILock producerSubmitterLock;
	private HazelcastCallableProducer<?> hazelcastCallableProducer;
	private HazelcastMemberRoundRobin hazelcastMemberRoundRobin;
	private HazelcastMemberRoundRobinExecutionCallback hazelcastMemberRoundRobinExecutionCallback;
	
	/**
     * Constructor for HazelcastCallableProducerSubmitterDetail.
     * @param configurationName configuration name to be used as roundrobin key. The roundrobin is per configuration
	 * @param hazelcastInstance hazelcast instance
	 * @param hazelcastCallableProducer callable producer
	 * @since 1.0.0
	 */
	public HazelcastCallableProducerSubmitterDetail(String configurationName, HazelcastInstance hazelcastInstance, HazelcastCallableProducer<?> hazelcastCallableProducer) {
		this.executorService = hazelcastInstance.getExecutorService(ClusterWorkerConstants.CW_EXECUTOR_SERVICE_NAME);
		this.producerSubmitterLock = hazelcastInstance.getLock(configurationName);
		this.hazelcastCallableProducer = hazelcastCallableProducer;
		this.hazelcastMemberRoundRobin = new HazelcastMemberRoundRobin(hazelcastInstance, String.format("%s[%s]", ClusterWorkerConstants.CW_ROUND_ROBIN_MEMBER, configurationName));
		this.hazelcastMemberRoundRobinExecutionCallback = new HazelcastMemberRoundRobinExecutionCallback(hazelcastMemberRoundRobin);
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
	 * Retrieves the lock for this producer submitter
	 * @return producer submitter lock
	 * @since 1.0.0
	 */
	public ILock getProducerSubmitterLock() {
		return producerSubmitterLock;
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
	 * Retrieves hazelcastMemberRoundRobin
	 * @return hazelcastMemberRoundRobin
	 * @since 1.0.0
	 */
	public HazelcastMemberRoundRobin getHazelcastMemberRoundRobin() {
		return hazelcastMemberRoundRobin;
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