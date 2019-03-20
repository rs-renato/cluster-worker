package br.gov.go.sefaz.clusterworker.core;

import java.io.Serializable;

/**
 * Markup class to {@link TaskProcess} specializations.
 * @param <T> type of this type process.
 */
public interface TaskProcess<T> extends Serializable{
	void process(T type);
}
