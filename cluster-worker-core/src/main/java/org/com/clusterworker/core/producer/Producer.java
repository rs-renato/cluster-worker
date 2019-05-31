package org.com.clusterworker.core.producer;

import java.util.Collection;

/**
 * Contract to producers implementations.
 * @author rs-renato
 * @since 1.0.0
 * @param <T> type of data to be consummed by this producer.
 */
public interface Producer<T>{

    /**
     * Produces a collection of T type.
     * @param items to produce.
     * @throws InterruptedException 
     * @since 1.0.0
     */
    void produce(Collection<T> items) throws InterruptedException;
}