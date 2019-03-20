package br.com.clusterworker.producer;

import java.util.Collection;

/**
 * Contract to producer implementations.
 * @param <T> type of this producer.
 */
public interface Producer<T>{

    /**
     * Produces a collection of T type.
     * @param types to produce.
     */
    void produce(Collection<T> types);
}