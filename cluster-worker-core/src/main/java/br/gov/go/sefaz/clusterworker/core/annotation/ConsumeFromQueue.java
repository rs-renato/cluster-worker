package br.gov.go.sefaz.clusterworker.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import br.gov.go.sefaz.clusterworker.core.item.ItemProcessor;
import br.gov.go.sefaz.clusterworker.core.queue.QueueStrategy;

/**
 * Annotation to configure a {@link ItemProcessor}.
 * @author renato-rs
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ConsumeFromQueue {

    /**
     * Queue name.
     * @return queueName
     */
    String queueName();

    /**
     * Strategy of consumption of item processor's queue. The default value is {@link QueueStrategy#ACCEPT_NULL}.
     * See: {@link QueueStrategy}
     * @return Strategy of consumption of item processor's queue.
     */
    QueueStrategy strategy() default QueueStrategy.ACCEPT_NULL;

    /**
     * Timeout of execution (in seconds) to the item processor before to return null on queue consumption.
     * If {@link QueueStrategy#WAIT_ON_AVAILABLE} strategy is defined, this property there is
     * no behavior, since this strategy is blocking. The default vaule is 01 second.
     * @return timeout
     */
    int timeout() default 1;

    /**
     * Number of workers (threads) to the processor. The default value is 01.
     * @return workers
     */
    int workers() default 1;
}
