package br.com.clusterworker.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import br.com.clusterworker.core.BaseProducer;


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
