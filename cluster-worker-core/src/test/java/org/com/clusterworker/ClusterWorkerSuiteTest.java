package org.com.clusterworker;

import org.com.clusterworker.core.ClusterWorkerTest;
import org.com.clusterworker.core.consumer.HazelcastQueueBlockingConsumerTest;
import org.com.clusterworker.core.consumer.HazelcastQueueNonBlockingConsumerTest;
import org.com.clusterworker.core.producer.HazelcastQueueProducerTest;
import org.com.clusterworker.core.roundrobin.HazelcastMemberRoundRobinTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Test Suite for Cluster Worker
 * @author rs-renato
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