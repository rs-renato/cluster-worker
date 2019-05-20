package br.gov.go.sefaz.clusterworker.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import br.gov.go.sefaz.clusterworker.core.item.ItemProducer;

/**
 * Annotation to configure a {@link ItemProducer}.
 * @author renato-rs
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ProduceToQueue {

    /**
     * Queue name.
     * @return queueName
     * @since 1.0.0
     */
    String queueName();
    
    /**
     * Max queue size. The default vaule is 1000 elements.
     * @return queue size
     * @since 1.0.0
     */
    int maxSize() default 1000;

    /**
     * Cron Expression to define the frequency of producer execution.
     * </br>Eg.: The expression <i><code>0/10 * * * * ?</code></i> executes at every 10 seconds.
     * @return cron expression
     * @since 1.0.0
     */
    String cronExpression();
}
