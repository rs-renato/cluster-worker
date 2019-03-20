package br.com.clusterworker.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to configure a {@literal TaskProduce}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TaskProduceConfig {

    /**
     * Queue name to the task produce.
     * @return queue name
     */
    String queueName();

    /**
     * Frequency of execution (in seconds) to the task produce.
     * The default vaule is 60 seconds.
     * @return timeout
     */
    int frequency() default 60;
}
