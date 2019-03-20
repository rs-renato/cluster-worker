package br.com.clusterworker.core.consumer.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import br.com.clusterworker.core.BaseConsumer;
import br.com.clusterworker.core.ClusterWorker;
import br.com.clusterworker.core.ClusterWorkerFactory;
import br.com.clusterworker.core.constants.TestConstants;
import br.com.clusterworker.core.task.impl.MyTaskProducer;

/**
 * Created by renato-rs on 21/10/2016.
 */
public class BaseConsumerTest {

    private static ClusterWorkerFactory cwFactory = ClusterWorkerFactory.getInstance();

    private static ClusterWorker<Integer> clusterWorker;
    private static BaseConsumer<Integer> baseConsumer;

    @BeforeClass
    public static void setUp(){
        clusterWorker = cwFactory.getClusterWorker(Integer.class);
        clusterWorker.executeTaskProduce(new MyTaskProducer());
    }

    @AfterClass
    public static void tearDownClass(){
        clusterWorker.shutDownAll();
    }

    @Test
    public void testWaitOnAvailableBaseConsumer(){

        baseConsumer = new MyWaitOnAvailableBaseConsumer();

       Integer result;

        while ((result = baseConsumer.consume()) != TestConstants.TASK_PRODUCE_QUANTITY){
            assertNotNull(result);
        }
    }

    @Test
    public void testAcceptNullBaseConsumer(){

        baseConsumer = new MyAcceptNullBaseConsumer();

        int loop = TestConstants.TASK_PRODUCE_QUANTITY * 5;

        int count =0;

        List<Integer> resultList = new ArrayList<Integer>();

        while (count < loop){
            resultList.add(baseConsumer.consume());
            count++;
        }

        assertTrue(resultList.contains(null));
    }
}