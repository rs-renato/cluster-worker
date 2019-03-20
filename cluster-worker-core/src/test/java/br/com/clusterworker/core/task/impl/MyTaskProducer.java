package br.com.clusterworker.core.task.impl;

import br.com.clusterworker.annotations.TaskProduceConfig;
import br.com.clusterworker.core.TaskProduceLockable;
import br.com.clusterworker.core.constants.TestConstants;
import br.com.clusterworker.lock.AtomicLock;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by renato-rs on 11/10/2016.
 */
@TaskProduceConfig(queueName = TestConstants.TASK_QUEUE, frequency = TestConstants.TASK_PRODUCE_FREQUENCY)
public class MyTaskProducer extends TaskProduceLockable<Integer> {

    private static final Logger logger = Logger.getLogger(MyTaskProducer.class);

    @Override
    public Collection<Integer> produce(AtomicLock atomicLock) {

        atomicLock.lock();

        List<Integer> list = new ArrayList<Integer>();

        try{

            for (int i = 1; i <= TestConstants.TASK_PRODUCE_QUANTITY; i++){
                list.add(i);
            }

            logger.info("Producing objects... " + list);

        }finally {
            atomicLock.unlock();
        }

        return list;
    }
}
