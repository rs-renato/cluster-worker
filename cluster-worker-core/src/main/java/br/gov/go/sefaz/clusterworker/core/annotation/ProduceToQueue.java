package br.gov.go.sefaz.clusterworker.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.quartz.CronExpression;

import br.gov.go.sefaz.clusterworker.core.item.ItemProducer;

/**
 * Annotation to configure a {@link ItemProducer}.
 * @author renato.rsilva
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
     * Max queue size to configure a boundary queue. The default vaule is 1000 elements.
     * @return queue size
     * @since 1.0.0
     */
    int maxSize() default 1000;

    /**
     * {@link CronExpression} to define the frequency of producer execution.
     * </br>Eg.: The expression <i><code>0/10 * * * * ?</code></i> executes at every 10 seconds.
     * @return cron expression
     * @since 1.0.0
     */
    String cronExpression();
}
