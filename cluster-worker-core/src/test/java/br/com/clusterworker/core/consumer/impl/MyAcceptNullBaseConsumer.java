package br.com.clusterworker.core.consumer.impl;

import br.com.clusterworker.annotations.BaseConsumerConfig;
import br.com.clusterworker.core.BaseConsumer;
import br.com.clusterworker.core.constants.TestConstants;
import br.com.clusterworker.utils.QueueStrategy;

/**
 * Created by renatorodrigues on 22/10/16.
 */
@BaseConsumerConfig(queueName = TestConstants.TASK_QUEUE, strategy = QueueStrategy.ACCEPT_NULL, timeout = TestConstants.BASE_CONSUMER_TIMEOUT)
public class MyAcceptNullBaseConsumer extends BaseConsumer<Integer> {

    @Override
    public Integer consume(){
        return super.consume();
    }
}
