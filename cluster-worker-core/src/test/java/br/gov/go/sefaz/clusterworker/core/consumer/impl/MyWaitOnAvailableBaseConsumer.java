package br.gov.go.sefaz.clusterworker.core.consumer.impl;

import br.gov.go.sefaz.clusterworker.core.BaseConsumer;
import br.gov.go.sefaz.clusterworker.core.annotations.BaseConsumerConfig;
import br.gov.go.sefaz.clusterworker.core.constants.TestConstants;
import br.gov.go.sefaz.clusterworker.core.utils.QueueStrategy;

/**
 * Created by renatorodrigues on 22/10/16.
 */
@BaseConsumerConfig(queueName = TestConstants.TASK_QUEUE, strategy = QueueStrategy.WAIT_ON_AVAILABLE)
public class MyWaitOnAvailableBaseConsumer extends BaseConsumer<Integer> {

	private static final long serialVersionUID = 6429769717447517853L;

	@Override
    public Integer consume(){
        return super.consume();
    }
}
