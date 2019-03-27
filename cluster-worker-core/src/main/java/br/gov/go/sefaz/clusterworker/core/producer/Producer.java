package br.gov.go.sefaz.clusterworker.core.producer;

import java.util.Collection;

/**
 * Contract to producers implementations.
 * @author renato-rs
 * @since 1.0
 * @param <T> type of data to be consummed by this producer.
 */
public interface Producer<T>{

    /**
     * Produces a collection of T type.
     * @param types to produce.
     */
    void produce(Collection<T> types);
}