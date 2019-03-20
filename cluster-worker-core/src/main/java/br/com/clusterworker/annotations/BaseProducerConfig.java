package br.com.clusterworker.annotations;

import br.com.clusterworker.core.BaseProducer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Annotation to configure {@link BaseProducer}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface BaseProducerConfig {

    /**
     * Queue name of this base producer.
     * @return queue name
     */
    String queueName();
}
