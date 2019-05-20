package br.gov.go.sefaz.clusterworker.core.roundrobin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.cluster.ClusterState;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;

/**
 * Hazelcast Member Round Robin strategy
 * @author renato-rs
 * @since 1.0.0
 */
@SuppressWarnings("deprecation")
public class HazelcastMemberRoundRobin{
	
	private static final Logger logger = LogManager.getLogger(HazelcastMemberRoundRobin.class);

    private final HazelcastInstance hazelcastInstance;
    private final String roundRobinName;
    
	public HazelcastMemberRoundRobin(HazelcastInstance hazelcastInstance, String roundRobinName) {
		this.hazelcastInstance = hazelcastInstance;
		this.roundRobinName = roundRobinName;
	}
	
	/**
	 * Advances the round robin pivot if and only if the cluster is active and its size is greather than 01
	 * @return HazelcastMemberRoundRobin
	 * @since 1.0.0
	 */
	public HazelcastMemberRoundRobin advance() {
		ClusterState clusterState = getClusterState();
		// Just update the pivot if the the cluster is active and has more than one member
		if (clusterState.equals(ClusterState.ACTIVE) && getClusterMembers().size() > 1) {
			logger.debug(String.format("Advancing round robin pivot '%s'", this.roundRobinName));
			hazelcastInstance.getAtomicLong(this.roundRobinName).incrementAndGet();
		}
		
        return this;
	}
	
	/**
	 * Return actual cluster member (round robin selected) from hazelcast instance 
	 * @return the cluster member
	 * @since 1.0.0
	 */
	public Member select() {
    	
    	Member member;

        List<Member> clusterMembers = new ArrayList<>(getClusterMembers());
        // Sorts the cluster members by its ID
        Collections.sort(clusterMembers, (one, other) -> one.toString().compareTo(other.toString()));
        
		ClusterState clusterState = getClusterState();
		int membersSize = clusterMembers.size();
		int count = (int) this.hazelcastInstance.getAtomicLong(this.roundRobinName).get();
		
		// Calculate module and get member index
		int selectedMemberIndex = count % membersSize;
		
		// If the cluster is not active or has just one member, return the first one
		if (!clusterState.equals(ClusterState.ACTIVE) || membersSize == 1) {
			logger.debug(String.format("The cluster state is %s. Cluster Size: %s - Selecting the oldest member (master)", clusterState, membersSize));
			member = clusterMembers.iterator().next();
		}else {
			member = clusterMembers.get(selectedMemberIndex);
		}

		logger.info(String.format("Roundrobin executed. Cluster Size: %s - Selected Member Index: %s - Count: %s - Member ID: %s", membersSize, selectedMemberIndex, count, member.getUuid()));
		
		return member;
    }
    
    /**
     * Retrieves the cluster state
     * @return the cluster state
     * @since 1.0.0
     */
    private ClusterState getClusterState() {
    	return this.hazelcastInstance.getCluster().getClusterState();
    }
    
    /**
     * Retrieves the cluster members
     * @return the cluster members
     * @since 1.0.0
     */
    private Set<Member> getClusterMembers(){
    	return this.hazelcastInstance.getCluster().getMembers();
    }
}
