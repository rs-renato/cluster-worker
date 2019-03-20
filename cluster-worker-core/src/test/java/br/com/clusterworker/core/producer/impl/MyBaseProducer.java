package br.com.clusterworker.core.producer.impl;

import br.com.clusterworker.annotations.BaseProducerConfig;
import br.com.clusterworker.core.BaseProducer;

import java.util.Collection;

/**
 * Created by renatorodrigues on 23/10/16.
 */ 
@BaseProducerConfig(queueName = "myQueue")
public class MyBaseProducer extends BaseProducer<Integer> {

    @Override
    public void produce(Collection<Integer> types) {
        super.produce(types);
    }
}
