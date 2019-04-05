package br.gov.go.sefaz.clusterworker.core.roundrobin;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.AfterClass;
import org.junit.Test;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;

/**
 * HazelcastMemberRoundRobin example of use
 * @author renato-rs
 * @since 1.0
 */
public class HazelcastMemberRoundRobinTest {

	@AfterClass
	public static void tearDownClass() {
		Hazelcast.shutdownAll();
	}
	
	@Test
    public void shouldRoundRobinMember() {
		int quantity = 3;
		// Creates and adds the hazelcast instances into the set
        Set<HazelcastInstance> hazelcastInscantes = factory(quantity, () -> Hazelcast.newHazelcastInstance(null));
        
        // Iterates the hazelcast instances and add the members into the set
        Set<Member> roundRobinMembers = factory(quantity, () -> HazelcastMemberRoundRobin.next(hazelcastInscantes.iterator().next(), "roundrobin"));
        
        // Asserts the quantity of members
        assert quantity == roundRobinMembers.size();

        // Iterate all instances, get the member, and add them to the set
        Set<Member> members = transformToSet(hazelcastInscantes, (hazelcastInstance) -> hazelcastInstance.getCluster().getLocalMember());
        
        // Asserts equals the original members to the roundrobin members
		assert roundRobinMembers.equals(members);
    }

    private <T> Set<T> factory(int quantity, Supplier<T> supplier) {
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