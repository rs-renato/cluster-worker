package br.gov.go.sefaz.clusterworker.core;

import java.util.Collection;

/**
 * Contract to task process on client's implementation.
 * @param <T> type of this task process.
 */
public abstract class TaskProcessUnlockable<T> extends TaskProcess<T> {

    /**
     *  Process an specific type on client's implementation.
     * @param type type to be processed.
     */
    protected abstract void process(T type);

    @Override
    final Collection<T> accept(TaskVisitor<T> taskVisitor) {
        taskVisitor.visit(this);
        return null;
    }
}
