package org.com.clusterworker.core.roundrobin;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import org.com.clusterworker.core.roundrobin.HazelcastMemberRoundRobin;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;


/**
 * HazelcastMemberRoundRobin example of use
 * @author rs-renato
 * @since 1.0.0
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HazelcastMemberRoundRobinTest {

	private static int INSTANCES_QUANTITY	= 3; 
	private static Set<HazelcastInstance> hazelcastInscantes;
	
	@BeforeClass
	public static void setUp() {
		// Creates and adds the hazelcast instances into the set
		hazelcastInscantes = factory(INSTANCES_QUANTITY, () -> Hazelcast.newHazelcastInstance(null));
	}
	
	@AfterClass
	public static void tearDownClass() {
		Hazelcast.shutdownAll();
	}
	
	@Test
    public void CW_T01_shouldRoundRobinMember() {
        
		HazelcastMemberRoundRobin hazelcastMemberRoundRobin = new HazelcastMemberRoundRobin(hazelcastInscantes.iterator().next(), "roundrobin");
		  
        // Iterates the hazelcast instances and add the members into the set
        Set<Member> roundRobinMembers = factory(INSTANCES_QUANTITY, () -> hazelcastMemberRoundRobin.advance().select());
        
        // Asserts the quantity of members
        Assert.assertEquals(INSTANCES_QUANTITY, roundRobinMembers.size());

        // Iterate all instances, get the member, and add them to the set
        Set<Member> members = transformToSet(hazelcastInscantes, (hazelcastInstance) -> hazelcastInstance.getCluster().getLocalMember());
        
        // Asserts equals the original members to the roundrobin members
        Assert.assertTrue(roundRobinMembers.equals(members));
    }

    private static <T> Set<T> factory(int quantity, Supplier<T> supplier) {
        Set<T> result = new HashSet<>();
        for(int i = 0; i < quantity; i++) {
            result.add(supplier.get());
        }
        return result;
    }

    private <T,E> Set<E> transformToSet(Iterable<T> elements, Function<T,E> function) {
        Set<E> result = new HashSet<>();
        for(T element : elements) {
            result.add(function.apply(element));
        }
        return result;
    }
}