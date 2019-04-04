package br.gov.go.sefaz.clusterworker.core.roundrobin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    	
    	Member member = null;

    	// Creates or obtain an atomic long
    	IAtomicLong iAtomicLong = hazelcastInstance.getAtomicLong(roundRobinName);
        List<Member> clusterMembers = new ArrayList<>(hazelcastInstance.getCluster().getMembers());
        int membersSize = clusterMembers.size();
        int selectedMemberIndex = 0;
        
        if(membersSize == 1) {
        	// Return the first member if the cluster has only one member
        	member = clusterMembers.get(selectedMemberIndex);
        }else {
            // Sorts the members by its ID and return the next member of cluster, in roundRobin 
        	Collections.sort(clusterMembers, (one, other) -> one.getUuid().compareTo(other.getUuid()));
        	selectedMemberIndex = (int) (iAtomicLong.incrementAndGet() % membersSize);
        	member = clusterMembers.get(selectedMemberIndex);
        }
        logger.debug(String.format("Executing roundrobing for '%s'. Cluster Members Size: %s - Selected Member Index: %s", roundRobinName, membersSize, selectedMemberIndex));
        return member;
    }
}
