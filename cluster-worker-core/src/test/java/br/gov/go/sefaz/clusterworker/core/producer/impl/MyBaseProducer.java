package br.gov.go.sefaz.clusterworker.core.producer.impl;

import java.util.Collection;

import br.gov.go.sefaz.clusterworker.core.BaseProducer;
import br.gov.go.sefaz.clusterworker.core.annotations.BaseProducerConfig;

/**
 * Created by renatorodrigues on 23/10/16.
 */ 
@BaseProducerConfig(queueName = "myQueue")
public class MyBaseProducer extends BaseProducer<Integer> {

	private static final long serialVersionUID = 7335958515274377289L;

	@Override
    public void produce(Collection<Integer> types) {
        super.produce(types);
    }
}
