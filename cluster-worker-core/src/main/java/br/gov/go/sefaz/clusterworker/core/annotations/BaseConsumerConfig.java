package br.gov.go.sefaz.clusterworker.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import br.gov.go.sefaz.clusterworker.core.consumer.BaseConsumer;
import br.gov.go.sefaz.clusterworker.core.utils.QueueStrategy;

/**
 * Annotation to configure a {@link BaseConsumer}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface BaseConsumerConfig {

    /**
     * Queue name to the base consumer.
     * @return queue name
     */
    String queueName();

    /**
     * Strategy to the base consumer.
     * See: {@link QueueStrategy}
     * @return strategy
     */
    QueueStrategy strategy();

    /**
     * Timeout of execution (in seconds) to the base consumer.
     * If the {@link QueueStrategy#WAIT_ON_AVAILABLE} is defined, this property there is
     * no behavior, since this strategy is blocking. The default vaule is 01 second.
     * @return timeout
     */
    int timeout() default 1;
}
