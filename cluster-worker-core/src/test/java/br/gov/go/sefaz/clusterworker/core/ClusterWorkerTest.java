package br.gov.go.sefaz.clusterworker.core;

import org.junit.Test;

import br.gov.go.sefaz.clusterworker.core.ClusterWorker;
import br.gov.go.sefaz.clusterworker.core.ClusterWorkerFactory;
import br.gov.go.sefaz.clusterworker.core.TaskProcessUnlockable;
import br.gov.go.sefaz.clusterworker.core.TaskProduceLockable;
import br.gov.go.sefaz.clusterworker.core.task.impl.MyTaskProcessor;
import br.gov.go.sefaz.clusterworker.core.task.impl.MyTaskProducer;

/**
 * Created by renato-rs on 04/10/2016.
 */
public class ClusterWorkerTest {

    @Test
    public void testClusterWorker() throws InterruptedException {

        TaskProduceLockable<Integer> taskProduce = new MyTaskProducer();
        TaskProcessUnlockable<Integer> taskProcessUnlockable = new MyTaskProcessor();

        ClusterWorker<Integer> clusterWorker = ClusterWorkerFactory.getInstance().getClusterWorker(Integer.class);

        clusterWorker.executeTaskProccess(taskProcessUnlockable);
        clusterWorker.executeTaskProduce(taskProduce);

        //executes for 10s
        Thread.sleep(10 * 1000);

        clusterWorker.shutDownAll();
    }
}