package br.gov.go.sefaz.clusterworker.core;

import java.io.Serializable;
import java.util.Collection;

/**
 * Visitor of {@TaskAcceptable} specializations.
 * @param <T> type of this visitor.
 */
interface TaskVisitor<T> extends Serializable{

    /**
     * Visit a {@link TaskProduceLockable}.
     * @param taskProduceLockable to be visited.
     * @return a collection of T
     */
    public Collection<T> visit(TaskProduceLockable<T> taskProduceLockable);

    /**
     * Visit a {@link TaskProduceUnlockable}.
     * @param taskProduceUnlockable to be visited.
     * @return a collection of T
     */
    public Collection<T> visit(TaskProduceUnlockable<T> taskProduceUnlockable);

    /**
     * Visit a {@link TaskProcessUnlockable}.
     * @param taskProcessUnlockable to be visited.
     */
    public void visit(TaskProcessUnlockable<T> taskProcessUnlockable);


    /**
     * Visit a {@link TaskProcessLockable}.
     * @param taskProcessLockable to be visited.
     */
    public void visit(TaskProcessLockable<T> taskProcessLockable);
}