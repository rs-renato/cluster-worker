package br.com.clusterworker.core;

import java.io.Serializable;
import java.util.Collection;

/**
 * Markup Class to {@link TaskProcess} and {@link TaskProduce}.
 * @param <T> type of this acceptable
 */
abstract class TaskAcceptable<T> implements Serializable {

    /**
     * Accepts the task visitor.
     * @param taskVisitor
     * @return collection of T if there is any response, otherwise returns null.
     */
    abstract Collection<T> accept(TaskVisitor<T> taskVisitor);
}