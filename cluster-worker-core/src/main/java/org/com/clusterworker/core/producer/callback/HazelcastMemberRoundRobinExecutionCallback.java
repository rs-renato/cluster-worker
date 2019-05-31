package org.com.clusterworker.core.producer.callback;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.com.clusterworker.core.roundrobin.HazelcastMemberRoundRobin;

import com.hazelcast.core.ExecutionCallback;

/**
 * Task Execution Callback responsible to advance the roundrobin pivot on success process
 * @author rs-renato
 * @since 1.0.0
 */
public class HazelcastMemberRoundRobinExecutionCallback implements ExecutionCallback<Void> {

	private static final Logger logger = LogManager.getLogger(HazelcastMemberRoundRobinExecutionCallback.class);

	private HazelcastMemberRoundRobin hazelcastMemberRoundRobin;

	/**
     * Constructor for HazelcastMemberRoundRobinExecutionCallback.
	 * @param hazelcastMemberRoundRobin
	 * @since 1.0.0
	 */
	public HazelcastMemberRoundRobinExecutionCallback(HazelcastMemberRoundRobin hazelcastMemberRoundRobin) {
		this.hazelcastMemberRoundRobin = hazelcastMemberRoundRobin;
	}

	@Override
	public void onResponse(Void response) {
		logger.trace("ExecutionCallback was succesfully executed!");
		hazelcastMemberRoundRobin.advance();
	}

	@Override
	public void onFailure(Throwable t) {
		logger.error(String.format("ExecutionCallback got an error: %s", t.getMessage()));
	}
}