package br.gov.go.sefaz.clusterworker.core.task;

import java.io.Serializable;
import java.util.Collection;

/**
 * Contract to task producer implementations.
 * @author renato-rs
 * @since 1.0
 * @param <T> type of data to be produced by this producer.
 */
public interface TaskProducer<T> extends Serializable {
	
	/**
	 * Produces items
	 * @return collection of item produced
	 */
	Collection<T> produce();
}
