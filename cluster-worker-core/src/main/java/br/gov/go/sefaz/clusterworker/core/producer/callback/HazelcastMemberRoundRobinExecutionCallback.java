package br.gov.go.sefaz.clusterworker.core.producer.callback;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.core.ExecutionCallback;

import br.gov.go.sefaz.clusterworker.core.roundrobin.HazelcastMemberRoundRobin;

/**
 * Task Execution Callback responsible to advance the roundrobin pivot on success process
 * @author renato-rs
 * @since 1.0
 */
public class HazelcastMemberRoundRobinExecutionCallback implements ExecutionCallback<Object> {

	private static final Logger logger = LogManager.getLogger(HazelcastMemberRoundRobinExecutionCallback.class);

	private HazelcastMemberRoundRobin hazelcastMemberRoundRobin;

	/**
     * Constructor for HazelcastMemberRoundRobinExecutionCallback.
	 * @param hazelcastMemberRoundRobin
	 * @since 1.0
	 */
	public HazelcastMemberRoundRobinExecutionCallback(HazelcastMemberRoundRobin hazelcastMemberRoundRobin) {
		this.hazelcastMemberRoundRobin = hazelcastMemberRoundRobin;
	}

	@Override
	public void onResponse(Object response) {
		logger.debug("ExecutionCallback was succesfully executed!");
		// Advances the roundrobin pivot
		hazelcastMemberRoundRobin.advance();
	}

	@Override
	public void onFailure(Throwable t) {
		logger.error(String.format("ExecutionCallback got an error: %s", t.getMessage()));
	}
}