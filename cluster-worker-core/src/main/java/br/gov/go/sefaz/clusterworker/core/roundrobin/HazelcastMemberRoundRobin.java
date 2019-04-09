package br.gov.go.sefaz.clusterworker.core.roundrobin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.cluster.ClusterState;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicLong;
import com.hazelcast.core.Member;

/**
 * Hazelcast Member Round Robin strategy
 * @author renato-rs
 * @since 1.0
 */
public class HazelcastMemberRoundRobin{
	
	private static final Logger logger = LogManager.getLogger(HazelcastMemberRoundRobin.class);

	private final IAtomicLong iAtomicLong;
    private final HazelcastInstance hazelcastInstance;
    
	public HazelcastMemberRoundRobin(HazelcastInstance hazelcastInstance, String roundRobinName) {
		this.iAtomicLong = hazelcastInstance.getAtomicLong(roundRobinName);
		this.hazelcastInstance = hazelcastInstance;
	}
	
	/**
	 * Advances the round robin pivot if and only if the cluster is active and its size is greather than 01
	 * @return HazelcastMemberRoundRobin
	 */
	public HazelcastMemberRoundRobin advance() {
		ClusterState clusterState = getClusterState();

		if (clusterState.equals(ClusterState.ACTIVE) && getClusterMembers().size() > 1) {
			logger.debug("Advancing round robin pivot");
			this.iAtomicLong.incrementAndGet();
		}
		
        return this;
	}
	
	/**
	 * Return actual cluster member (round robin selected) from hazelcast instance 
	 * @return the cluster member
	 */
    public Member select() {
    	
    	Member member;

        List<Member> clusterMembers = new ArrayList<>(getClusterMembers());
        Collections.sort(clusterMembers, (one, other) -> one.toString().compareTo(other.toString()));
        
		ClusterState clusterState = getClusterState();
		int membersSize = clusterMembers.size();
		int count = (int) iAtomicLong.get();
		
		int selectedMemberIndex = count % membersSize;
		
		if (!clusterState.equals(ClusterState.ACTIVE) || membersSize == 1) {
			logger.debug(String.format("The cluster state is %s. Cluster Size: %s - Selecting the oldest member (master)", clusterState, membersSize));
			member = clusterMembers.iterator().next();
		}else {
			member = clusterMembers.get(selectedMemberIndex);
		}

		logger.info(String.format("Roundrobing executed. Cluster Size: %s - Selected Member Index: %s - Count: %s - Member ID: %s", membersSize, selectedMemberIndex, count, member.getUuid()));
		
		return member;
    }
    
    /**
     * Retrieves the cluster state
     * @return the cluster state
     */
    private ClusterState getClusterState() {
    	return this.hazelcastInstance.getCluster().getClusterState();
    }
    
    /**
     * Retrieves the cluster members
     * @return the cluster members
     */
    private Set<Member> getClusterMembers(){
    	return this.hazelcastInstance.getCluster().getMembers();
    }
}
