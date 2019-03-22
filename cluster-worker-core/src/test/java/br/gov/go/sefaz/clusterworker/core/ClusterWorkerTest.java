package br.gov.go.sefaz.clusterworker.core;

import org.junit.Test;

import br.gov.go.sefaz.clusterworker.core.factory.ClusterWorkerFactory;
import br.gov.go.sefaz.clusterworker.core.task.IntegerTaskProcessor;
import br.gov.go.sefaz.clusterworker.core.task.IntegerTaskProducer;

/**
 * Created by renato-rs on 04/10/2016.
 */
public class ClusterWorkerTest {

    @Test
    public void testClusterWorker() throws InterruptedException {

        ClusterWorker<Integer> clusterWorker = ClusterWorkerFactory.getInstance().getClusterWorker(Integer.class);

        clusterWorker.executeTaskProducer(new IntegerTaskProducer());
        clusterWorker.executeTaskProccessor(new IntegerTaskProcessor());

        //executes for 10s
        Thread.sleep(60 * 1000);

        clusterWorker.shutdown();
    }
}