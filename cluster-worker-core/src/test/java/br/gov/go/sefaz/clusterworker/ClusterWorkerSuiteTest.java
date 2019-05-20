package br.gov.go.sefaz.clusterworker;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import br.gov.go.sefaz.clusterworker.core.ClusterWorkerTest;
import br.gov.go.sefaz.clusterworker.core.consumer.HazelcastQueueBlockingConsumerTest;
import br.gov.go.sefaz.clusterworker.core.consumer.HazelcastQueueNonBlockingConsumerTest;
import br.gov.go.sefaz.clusterworker.core.producer.HazelcastQueueProducerTest;
import br.gov.go.sefaz.clusterworker.core.roundrobin.HazelcastMemberRoundRobinTest;

/**
 * Test Suite for Cluster Worker
 * @author renato-rs
 * @since 1.0.0
 */
@RunWith(Suite.class)
@SuiteClasses({ClusterWorkerTest.class, 
	HazelcastQueueBlockingConsumerTest.class,
	HazelcastQueueNonBlockingConsumerTest.class,
	HazelcastQueueProducerTest.class,
	HazelcastMemberRoundRobinTest.class})
public class ClusterWorkerSuiteTest {

}