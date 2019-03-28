package br.gov.go.sefaz.clusterworker.core.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import br.gov.go.sefaz.clusterworker.core.ClusterWorkerTest;
import br.gov.go.sefaz.clusterworker.core.consumer.HazelcastQueueBlockingConsumerTest;
import br.gov.go.sefaz.clusterworker.core.consumer.HazelcastQueueNonBlockingConsumerTest;
import br.gov.go.sefaz.clusterworker.core.producer.HazelcastQueueProducerTest;

@RunWith(Suite.class)
@SuiteClasses({ClusterWorkerTest.class, 
	HazelcastQueueBlockingConsumerTest.class,
	HazelcastQueueNonBlockingConsumerTest.class,
	HazelcastQueueProducerTest.class})
public class ClusterWorkerSuiteTest {

}
