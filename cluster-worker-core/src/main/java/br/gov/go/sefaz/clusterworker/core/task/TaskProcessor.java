package br.gov.go.sefaz.clusterworker.core.task;

import java.io.Serializable;

/**
 * Markup class to {@link TaskProcessor} specializations.
 * @param <T> type of this type process.
 */
public interface TaskProcessor<T> extends Serializable{
	void process(T type);
}
