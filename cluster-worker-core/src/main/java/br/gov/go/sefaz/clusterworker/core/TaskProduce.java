package br.gov.go.sefaz.clusterworker.core;

import java.io.Serializable;
import java.util.Collection;

/**
 * Markup class to {@link TaskProduce} specializations.
 * @param <T> type of this type produce.
 */
public interface TaskProduce<T> extends Serializable {
	Collection<T> produce();
}
