package br.gov.go.sefaz.clusterworker.core.producer.impl;

import java.util.Collection;

import br.gov.go.sefaz.clusterworker.core.producer.HazelcastQueueeProducer;

/**
 * Created by renatorodrigues on 23/10/16.
 */ 
public class MyBaseProducer extends HazelcastQueueeProducer<Integer> {

	private static final long serialVersionUID = 7335958515274377289L;

	public MyBaseProducer() {
		super("myQueue");
	}
	
	@Override
    public void produce(Collection<Integer> types) {
        super.produce(types);
    }
}
