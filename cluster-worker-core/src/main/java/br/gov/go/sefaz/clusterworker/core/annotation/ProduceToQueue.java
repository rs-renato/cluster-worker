package br.gov.go.sefaz.clusterworker.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import br.gov.go.sefaz.clusterworker.core.item.ItemProducer;

/**
 * Annotation to configure a {@link ItemProducer}.
 * @author renato-rs
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ProduceToQueue {

    /**
     * Queue name.
     * @return queueName
     */
    String queueName();

    /**
     * Frequency of execution (in seconds) to the item producer. The default vaule is 60 seconds.
     * @return timeout
     */
    int frequency() default 60;
}
