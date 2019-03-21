package br.gov.go.sefaz.clusterworker.core.consumer.impl;

import br.gov.go.sefaz.clusterworker.core.constants.TestConstants;
import br.gov.go.sefaz.clusterworker.core.consumer.HazelcastQueueeConsumer;
import br.gov.go.sefaz.clusterworker.core.utils.QueueStrategy;

/**
 * Created by renatorodrigues on 22/10/16.
 */
public class MyWaitOnAvailableBaseConsumer extends HazelcastQueueeConsumer<Integer> {

	private static final long serialVersionUID = 6429769717447517853L;
	
	public MyWaitOnAvailableBaseConsumer() {
		super(TestConstants.TASK_QUEUE, QueueStrategy.WAIT_ON_AVAILABLE);
	}

	@Override
    public Integer consume(){
        return super.consume();
    }
}
