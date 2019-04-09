package br.gov.go.sefaz.clusterworker.core.roundrobin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.cluster.ClusterState;
import com.hazelcast.core.Cluster;
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
	 * Advances the round robin pivot
	 * @return HazelcastMemberRoundRobin
	 */
	public HazelcastMemberRoundRobin advance() {
		logger.debug("Advancing round robin pivot");
		this.iAtomicLong.incrementAndGet();
        return this;
	}
	
	/**
	 * Return actual cluster member (round robin selected) from hazelcast instance 
	 * @return the cluster member
	 */
    public Member select() {
    	
    	Member member;

        Cluster cluster = hazelcastInstance.getCluster();
        List<Member> clusterMembers = new ArrayList<>(cluster.getMembers());
        Collections.sort(clusterMembers, (one, other) -> one.toString().compareTo(other.toString()));
        
        // The master member (the oldest member in the cluster)
        Member masterMember = clusterMembers.iterator().next();
        
		ClusterState clusterState = cluster.getClusterState();
		int membersSize = clusterMembers.size();
		
		int count = (int) iAtomicLong.get();
		int selectedMemberIndex = count % membersSize;
		
		if (!clusterState.equals(ClusterState.ACTIVE) || membersSize == 1) {
			logger.debug(String.format("The cluster state is %s. Cluster Size: %s - Selecting the oldest member (master)", clusterState, membersSize));
			member = masterMember;
		}else {
			member = clusterMembers.get(selectedMemberIndex);
		}

		logger.info(String.format("Roundrobing executed. Cluster Size: %s - Selected Member Index: %s - Count: %s - Member ID: %s", membersSize, selectedMemberIndex, count, member.getUuid()));
		
		return member;
    }
}
