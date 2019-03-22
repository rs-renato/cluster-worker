package br.gov.go.sefaz.clusterworker.core;

import org.junit.Test;

import br.gov.go.sefaz.clusterworker.core.factory.ClusterWorkerFactory;
import br.gov.go.sefaz.clusterworker.core.task.TaskProcessor;
import br.gov.go.sefaz.clusterworker.core.task.TaskProducer;
import br.gov.go.sefaz.clusterworker.core.task.impl.MyTaskProcessor;
import br.gov.go.sefaz.clusterworker.core.task.impl.MyTaskProducer;

/**
 * Created by renato-rs on 04/10/2016.
 */
public class ClusterWorkerTest {

    @Test
    public void testClusterWorker() throws InterruptedException {

        TaskProducer<Integer> taskProduce = new MyTaskProducer();
        TaskProcessor<Integer> taskProcessUnlockable = new MyTaskProcessor();

        ClusterWorker<Integer> clusterWorker = ClusterWorkerFactory.getInstance().getClusterWorker(Integer.class);

        clusterWorker.executeTaskProccessor(taskProcessUnlockable);
        clusterWorker.executeTaskProducer(taskProduce);

        //executes for 10s
        Thread.sleep(60 * 1000);

        clusterWorker.shutDownAll();
    }
}