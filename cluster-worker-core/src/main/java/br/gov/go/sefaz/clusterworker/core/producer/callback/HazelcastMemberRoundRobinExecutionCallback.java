package br.gov.go.sefaz.clusterworker.core.producer.callback;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.core.ExecutionCallback;

import br.gov.go.sefaz.clusterworker.core.roundrobin.HazelcastMemberRoundRobin;

/**
 * Task Execution Callback responsible to advance the roundrobin pivot on success process
 * @author renato.rsilva
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
		logger.debug("ExecutionCallback was succesfully executed!");
		hazelcastMemberRoundRobin.advance();
	}

	@Override
	public void onFailure(Throwable t) {
		logger.error(String.format("ExecutionCallback got an error: %s", t.getMessage()));
	}
}