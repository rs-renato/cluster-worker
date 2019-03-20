package br.gov.go.sefaz.clusterworker.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import br.gov.go.sefaz.clusterworker.core.utils.QueueStrategy;


/**
 * Annotation to configure a {@literal TaskProcess}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TaskProcessConfig {

    /**
     * Queue name to the task process.
     * @return queue name
     */
    String queueName();

    /**
     * Strategy to the task process.
     * See: {@link QueueStrategy}
     * @return strategy
     */
    QueueStrategy strategy();

    /**
     * Timeout of execution (in seconds) to the task process.
     * If the {@link QueueStrategy#WAIT_ON_AVAILABLE} is defined, this property there is
     * no behavior, since this strategy is blocking. The default vaule is 01 second.
     * @return timeout
     */
    int timeout() default 1;

    /**
     * Number of workers to the process. The default value is 01 core.
     * @return workers
     */
    int workers() default 1;
}
