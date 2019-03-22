package br.gov.go.sefaz.clusterworker.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import br.gov.go.sefaz.clusterworker.core.consumer.ConsumerStrategy;
import br.gov.go.sefaz.clusterworker.core.task.TaskProcessor;

/**
 * Annotation to configure a {@link TaskProcessor}.
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
     * Strategy of consumption of task processor's queue. The default value is {@link ConsumerStrategy#ACCEPT_NULL}.
     * See: {@link ConsumerStrategy}
     * @return Strategy of consumption of task processor's queue.
     */
    ConsumerStrategy strategy() default ConsumerStrategy.ACCEPT_NULL;

    /**
     * Timeout of execution (in seconds) to the task processor before to return null on queue consumption.
     * If {@link ConsumerStrategy#WAIT_ON_AVAILABLE} strategy is defined, this property there is
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
