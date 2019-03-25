package br.gov.go.sefaz.clusterworker.core.task;

import java.io.Serializable;

/**
 * Contract to task processor implementations.
 * @author renato-rs
 * @since 1.0
 * @param <T> type of data to be processed by this processor.
 */
public interface TaskProcessor<T> extends Serializable{
	
	/**
	 * Process the item
	 * @param type to be processed
	 */
	void process(T type);
}
