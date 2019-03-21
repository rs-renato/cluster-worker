package br.gov.go.sefaz.clusterworker.core.consumer.impl;

import br.gov.go.sefaz.clusterworker.core.constants.TestConstants;
import br.gov.go.sefaz.clusterworker.core.consumer.HazelcastQueueeConsumer;
import br.gov.go.sefaz.clusterworker.core.utils.QueueStrategy;

/**
 * Created by renatorodrigues on 22/10/16.
 */
public class MyAcceptNullBaseConsumer extends HazelcastQueueeConsumer<Integer> {

	private static final long serialVersionUID = -4256596070059046676L;

	public MyAcceptNullBaseConsumer() {
		super(TestConstants.TASK_QUEUE, QueueStrategy.ACCEPT_NULL, TestConstants.BASE_CONSUMER_TIMEOUT);
	}
	
	@Override
    public Integer consume(){
        return super.consume();
    }
}
