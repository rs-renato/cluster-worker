package br.gov.go.sefaz.clusterworker.core.roundrobin;

import java.util.ArrayList;
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

	private HazelcastMemberRoundRobin() {
	}

	/**
	 * Return the next clustert member from hazelcast instance 
	 * @param hazelcastInstance hazelcast instance
	 * @param roundRobinName the name of roundrobin. This name is used to create a {@link IAtomicLong} to control the roundrobin strategy
	 * @return the next member
	 */
    public static Member next(HazelcastInstance hazelcastInstance, String roundRobinName) {
    	Member member;

    	// Creates or obtain an atomic long
    	IAtomicLong iAtomicLong = hazelcastInstance.getAtomicLong(roundRobinName);
        Cluster cluster = hazelcastInstance.getCluster();
		List<Member> clusterMembers = new ArrayList<>(cluster.getMembers());
        int membersSize = clusterMembers.size();
        int selectedMemberIndex;
		int count;

		count = (int) iAtomicLong.get();
		selectedMemberIndex = count % membersSize;
		member = clusterMembers.get(selectedMemberIndex);

		// Only the master member (the oldest member in the cluster) will update the count to grant sync
		Member masterMember = clusterMembers.iterator().next();
		if (masterMember.localMember() && cluster.getClusterState().equals(ClusterState.ACTIVE)){
			iAtomicLong.incrementAndGet();
		}

		logger.debug(String.format("Executing roundrobing for '%s'. Cluster Members Size: %s - Selected Member Index: %s - Count: %s", roundRobinName, membersSize, selectedMemberIndex, count));
        return member;
    }
}
