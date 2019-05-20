package br.gov.go.sefaz.clusterworker.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import br.gov.go.sefaz.clusterworker.core.consumer.ConsumerStrategy;
import br.gov.go.sefaz.clusterworker.core.item.ItemProcessor;

/**
 * Annotation to configure a {@link ItemProcessor}.
 * @author renato.rsilva
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ConsumeFromQueue {

    /**
     * Queue name.
     * @return queueName
     * @since 1.0.0
     */
    String queueName();

    /**
     * Strategy of consumption of item processor's queue. The default value is {@link ConsumerStrategy#ACCEPT_NULL}.
     * @see {@link ConsumerStrategy}
     * @return Strategy of consumption of item processor's queue.
     * @since 1.0.0
     */
    ConsumerStrategy strategy() default ConsumerStrategy.ACCEPT_NULL;

    /**
     * Timeout of execution (in seconds) to the item processor before to return null on queue consumption.
     * If {@link ConsumerStrategy#WAIT_ON_AVAILABLE} strategy is defined, this property there is
     * no behavior, since this strategy is blocking. The default vaule is 01 second.
     * @return timeout
     * @since 1.0.0
     */
    int timeout() default 1;

    /**
     * Number of workers (threads) to the processor. The default value is 01 workers.
     * @return workers
     * @since 1.0.0
     */
    int workers() default 1;
}
