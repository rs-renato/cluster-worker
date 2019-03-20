package br.gov.go.sefaz.clusterworker.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import br.gov.go.sefaz.clusterworker.core.producer.BaseProducer;


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
