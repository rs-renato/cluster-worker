package br.gov.go.sefaz.clusterworker.core.producer.quartz;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;

import br.gov.go.sefaz.clusterworker.core.constants.ClusterWorkerConstants;
import br.gov.go.sefaz.clusterworker.core.producer.HazelcastRunnableProducer;
import br.gov.go.sefaz.clusterworker.core.producer.callback.HazelcastMemberRoundRobinExecutionCallback;
import br.gov.go.sefaz.clusterworker.core.roundrobin.HazelcastMemberRoundRobin;

/**
 * Configuration for Quartz HazelcastRunnableProducer Submitter 
 * @author renato-rs
 * @since 1.0
 */
public class HazelcastRunnableProducerSubmitterConfiguration<T>{
	
	private HazelcastMemberRoundRobin hazelcastMemberRoundRobin;
	private HazelcastRunnableProducer<T> hazelcastRunnableProducer;
	private IExecutorService executorService;
	private HazelcastMemberRoundRobinExecutionCallback hazelcastMemberRoundRobinExecutionCallback;
	
	/**
     * Constructor for HazelcastRunnableProducerSubmitterConfiguration.
	 * @param hazelcastInstance hazelcast instance
	 * @param executorService executor service
	 * @param hazelcastRunnableProducer runnable producer
	 * @since 1.0
	 */
	public HazelcastRunnableProducerSubmitterConfiguration(HazelcastInstance hazelcastInstance, IExecutorService executorService, HazelcastRunnableProducer<T> hazelcastRunnableProducer) {
		this.executorService =executorService;
		this.hazelcastRunnableProducer = hazelcastRunnableProducer;
		this.hazelcastMemberRoundRobin = new HazelcastMemberRoundRobin(hazelcastInstance, ClusterWorkerConstants.CW_ROUND_ROBIN_MEMBER);
		this.hazelcastMemberRoundRobinExecutionCallback = new HazelcastMemberRoundRobinExecutionCallback(hazelcastMemberRoundRobin);
	}
	
	/**
	 * Retrieves hazelcastMemberRoundRobin
	 * @return hazelcastMemberRoundRobin
	 * @since 1.0
	 */
	public HazelcastMemberRoundRobin getHazelcastMemberRoundRobin() {
		return hazelcastMemberRoundRobin;
	}


	/**
	 * Retrieves HazelcastRunnableProducer
	 * @return hazelcastRunnableProducer
	 * @since 1.0
	 */
	public HazelcastRunnableProducer<?> getHazelcastRunnableProducer() {
		return hazelcastRunnableProducer;
	}

	/**
	 * Retrieves IExecutorService
	 * @return executorService
	 * @since 1.0
	 */
	public IExecutorService getExecutorService() {
		return executorService;
	}

	/**
	 * Retrieves HazelcastMemberRoundRobinExecutionCallback
	 * @return executionCallback
	 * @since 1.0
	 */
	public HazelcastMemberRoundRobinExecutionCallback getHazelcastMemberRoundRobinExecutionCallback() {
		return hazelcastMemberRoundRobinExecutionCallback;
	}
}