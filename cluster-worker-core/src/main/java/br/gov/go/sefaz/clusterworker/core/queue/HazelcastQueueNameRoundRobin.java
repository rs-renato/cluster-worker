package br.gov.go.sefaz.clusterworker.core.queue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.hazelcast.core.HazelcastInstance;

import br.gov.go.sefaz.clusterworker.core.constants.ClusterWorkerConstants;

/**
 * Implementation of RoundRobin for hazelcast queues
 * @author renato-rs
 * @since 1.0
 */
public class HazelcastQueueNameRoundRobin implements Serializable {

	private static final long serialVersionUID = -8083948985126604857L;

	public enum STRATEGY {
		ACCEPT_ONLY_EMPTY,
		IGNORE_IF_EMPTY
	}
	
	private int currentIndex;
	private int lastIndex;
	
	private String lastQueueName;
	
    private List<String> queueNames;

    public HazelcastQueueNameRoundRobin(String queueName) {
    	this(queueName, ClusterWorkerConstants.CW_QUEUE_SPLIT_SIZE);
    }
    
    public HazelcastQueueNameRoundRobin(String queueName, int sizeToSplit) {
    	this.queueNames = new ArrayList<>();
    	for (int i = 0; i < sizeToSplit; i++) {
			this.queueNames.add(queueName + "-" + i);
		}
    	
    	this.currentIndex = 0;
    	this.lastIndex = this.queueNames.size() -1;
    	this.lastQueueName = this.queueNames.get(this.lastIndex);
    }
    
    public boolean isLastQueue() {
		return queueNames.get(this.lastIndex).equals(this.lastQueueName);
	}
    
	public String nextQueue() {
		this.currentIndex = this.currentIndex % queueNames.size();
		this.lastIndex = this.currentIndex;
		return queueNames.get(this.currentIndex++);
	}
	
	public String lastQueue() {
		return queueNames.get(this.lastIndex);
	}

	public String nextHazelcastQueue(HazelcastInstance hazelcastInstance, STRATEGY strategy) {
		
		boolean ignoreIfEmpty = strategy.equals(STRATEGY.IGNORE_IF_EMPTY);
		
		for (int i = 0; i < queueNames.size(); i++) {
			
			String queueName = nextQueue();
			
			if (ignoreIfEmpty || hazelcastInstance.getQueue(queueName).isEmpty()) {
				return queueName;
			}
		}
		
		return null;
	}
}
