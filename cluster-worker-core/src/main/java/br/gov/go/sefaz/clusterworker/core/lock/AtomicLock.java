package br.gov.go.sefaz.clusterworker.core.lock;

import java.io.Serializable;

import org.apache.log4j.Logger;

import com.hazelcast.core.HazelcastInstance;

/**
 * Class that represents an atomic lock in the entire cluster.
 */
public class AtomicLock implements Serializable {

    private static final transient Logger logger = Logger.getLogger(AtomicLock.class);

    private transient HazelcastInstance hazelcastInstance;

    private String lockId;

    public AtomicLock(HazelcastInstance hazelcastInstance, String queueName, LockType lockType) {
        this.hazelcastInstance = hazelcastInstance;
        this.lockId = new StringBuilder(hazelcastInstance.getName())
                .append(".").append(queueName).append(lockType).toString();
    }

    /**
     * Locks the the entire cluster to this lockId.
     */
    public void lock() {
        hazelcastInstance.getLock(lockId).lock();
        logger.debug(String.format("%s locked!", lockId));
    }

    /**
     * Unlocks this the entire cluster waiting on this lockId.
     */
    public void unlock() {
        hazelcastInstance.getLock(lockId).unlock();
        logger.debug(String.format("%s unlocked!", lockId));
    }
}
