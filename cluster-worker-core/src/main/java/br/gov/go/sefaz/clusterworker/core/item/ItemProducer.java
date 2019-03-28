package br.gov.go.sefaz.clusterworker.core.item;

import java.io.Serializable;
import java.util.Collection;

/**
 * Contract to item producer implementations.
 * @author renato-rs
 * @since 1.0
 * @param <T> type of data to be produced by this producer.
 */
public interface ItemProducer<T> extends Serializable {
	
	/**
	 * Produces items
	 * @return collection of items produced
	 */
	Collection<T> produce();
}
