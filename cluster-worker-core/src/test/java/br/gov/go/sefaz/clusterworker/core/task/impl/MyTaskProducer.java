package br.gov.go.sefaz.clusterworker.core.task.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import br.gov.go.sefaz.clusterworker.core.TaskProduceLockable;
import br.gov.go.sefaz.clusterworker.core.annotations.TaskProduceConfig;
import br.gov.go.sefaz.clusterworker.core.constants.TestConstants;
import br.gov.go.sefaz.clusterworker.core.lock.AtomicLock;

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
