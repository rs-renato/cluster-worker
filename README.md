# Cluster Worker - *Scale your items easily.* [![Build Status](https://travis-ci.org/rs-renato/cluster-worker.svg?branch=master)](https://travis-ci.org/rs-renato/cluster-worker)
###### *This version is snapshot and still in development. Some documentation inconsistence could be found here.*
---

*Table of Content*
- [Cluster Worker - *Scale your items easily.*]
  * [The Client Perspective](#the-client-perspective)
    + [`Item Produce`](#item-produce)
    + [`Item Process`](#item-process)
  * [Executing Items](#executing-items)
    + [`ClusterWorker`](#clusterworker)
    + [Standalones: *Base Producers & Base Consumers*]
      - [`BaseProducer`](#baseproducer)
      - [`BaseConsumer`](#baseconsumer)
    + [AtomicLock](#atomiclock)
    + [QueueStrategy](#queuestrategy)
  * [The API Perspective](#the-api-perspective)
    + [Workers: *Producer & Consumer*]
      - [`WorkerProducer`](#workerproducer)
      - [`WorkerConsumer`](#workerconsumer)

Cluster Worker (CW) is a Hazelcast based API that help you to scale yours items producing and items processing under a cluster environment. CW uses producer x consumer strategy on hazelcast queues as central mechanism to distribute in an easily way the client's items implementations to be executed in the nodes, providing high availability and scalability for processing and exchange data through the cluster members.

<p align="center">
        <img alt="Cluster Worker Diagram" src="https://github.com/rs-renato/repository-images/blob/master/cluster-worker/cw_diagram.jpg?raw=true">
</p>
----------------------------------------------------------------------------------------------------------------------------------------

Before to start, let's see an overview from Hazelcast's web site:
>Hazelcast is an open source In-Memory Data Grid (IMDG). It provides elastically scalable distributed In-Memory computing, widely recognized as the fastest and most scalable approach to application performance. Hazelcast does this in open source and provides highly scalable and available (100% operational, never failing). Distributed applications can use Hazelcast for distributed caching, synchronization, clustering, processing, pub/sub messaging, etc. 

>Source: [https://hazelcast.org](https://hazelcast.org)

## The Client Perspective  

<p align="center">
        <img alt="ItemProduce" src="https://github.com/rs-renato/repository-images/blob/master/cluster-worker/itemAcceptable.jpg?raw=true" height="200"/>
</p>
----------------------------------------------------------------------------------------------------------------------------------------

Cluster Worker knows how to manage client's items, everything you need is provide an implementation for producing and processing data.
CW was designed to be item based, and comes in two flavors:

### `Item Produce`
Producer is a *mandatory* single item (managed by `WorkerProducer`) that is located in the entire cluster's node, but only one execution will be activated at time. This item is responsible to execute the client's production, it could produce from files, web-services, database or whatever kind of production source as you need.

*Note: You can have as many different item produce as you need, but in a cluster environment this item will produce only in one cluster's node to ensure the data won't be produced repeatedly and cause inconsistent processing. If a node fails, there is no problem, this item produce is redundant beyond the entire cluster nodes and executed in a random node to ensure HA producing.*

The example bellow shows an example of `ItemProduce` implementation that produces a Collection of 10 Integers to the queue named `myQueue` in a frequency of 05 seconds and receives an `AtomicLock` on the `produce()` method:

```java
@ItemProduceConfig(queueName = TestConstants.ITEM_QUEUE, frequency = TestConstants.ITEM_PRODUCE_FREQUENCY)
public class MyItemProducer extends ItemProduceLockable<Integer> {

    private static final Logger logger = Logger.getLogger(MyItemProducer.class);

    @Override
    public Collection<Integer> produce(AtomicLock atomicLock) {

        atomicLock.lock();

        List<Integer> list = new ArrayList<Integer>();

        try{


            for (int i = 1; i <= TestConstants.ITEM_PRODUCE_QUANTITY; i++){
                list.add(i);
            }

            logger.info("Producing objects... " + list);

        }finally {
            atomicLock.unlock();
        }

        return list;
    }
}
```
*Note:* `ItemProduce` has two specializations: `ItemProduceLockable` and `ItemProduceUnlockable`. Whereas ItemProduceUnlockable doesn't allows locks, ItemProduceLockable provides a way for control flow,
 in the cuncurrent environment through of `AtomicLock` object, which provides two methods `atomicLock.lock()` and `atomicLock.unlock()`.
        
### `Item Process`
Processors are *optional* items (managed by `WorkerConsumer`) that is allocated in all cluster's node (multithread per node). This item is responsible to execute the client's processing, it could be process to files (eg. exporting a xml/json), database, web-services, pdf generation, etc.

The example below shows an example of `ItemProcess` that process one Integer obtained from queue named `myQueue`, with strategy defined to wait until an Integer become available and working with 02 workers (Threads):

```java
@ItemProcessConfig(queueName = "myQueue", strategy = QueueStrategy.WAIT_ON_AVAILABLE, workers = 2)
public class MyItemProcessor extends ItemProcessUnlockable<Integer> {

    private static final Logger logger = Logger.getLogger(MyItemProcessor.class);

    @Override
    public void process(Integer type) {
        logger.info("Processing object " + type);
    }
}
```
*Note:* `ItemProcess` has two specializations: `ItemProcessLockable` and `ItemProcessUnlockable`. Whereas ItemProcessUnlockable doesn't allows locks, ItemProcessLockable provides a way for control flow,
 in the cuncurrent environment through of `AtomicLock` object, which provides two methods `atomicLock.lock()` and `atomicLock.unlock()`.

## Executing Items

### `ClusterWorker`
`ClusterWorker` class is the manager of `itemProduce` and `itemProcess`. An instance of CW for item's execution can be obtained as follow:

```java
    ClusterWorker<Integer> clusterWorker = ClusterWorkerFactory.getInstance().getClusterWorker(Integer.class);

    clusterWorker.executeItemProccess(itemProduce);
    clusterWorker.executeItemProduce(itemProcess);
```

This `ClusterWorker` will manage items producing and processing Integer objects through the cluster nodes.

### Standalones: *Base Producers & Base Consumers*
Cluster Worker allows you have an out of the box approach to control on demand your producing and consuming logic. You can manage when to produce and when to consume data directly from Hazelcast queue. Everything you need is to implement a standalone base producer/consumer with a composition of `BaseProducer` or `BaseConsumer`.

<p align="center">
        <img alt="ItemProduce" src="https://github.com/rs-renato/repository-images/blob/master/cluster-worker/baseProducer_baseConsumer.jpg?raw=true" height="200"/>
</p>
----------------------------------------------------------------------------------------------------------------------------------------

#### `BaseProducer` 
This base producer is useful when you need to control your producing process just by calling a `produce()` method. This approach access the `queue` directly and `put` data in it to be accessed by any `Consumer` of this `queue`: 
The example below shows a standalone producer which produces Integers to the queue named `myQueue`:

```java
@BaseProducerConfig(queueName = "myQueue")
public class MyBaseProducer extends BaseProducer<Integer> {

    @Override
    public void produce(Collection<Integer> types) {
        super.produce(types);
    }
}
```

The production on demand based is showed as follow:

```java

    BaseProducer<Integer> baseProducer = new StandaloneProducer();
    
    List<Intger> list = new ArrayList();
    list.add(1);
    list.add(2);
    
    baseProducer.produce(list);
```

#### `BaseConsumer`
This base consumer is useful when you need to control your consuming process just by calling a `consume()` method. This approach access the `queue` directly and `take` or `pool` data from it, according with the defined `QueueStrategy`:
The example below shows a standalone consumer which consume Integers from the queue named `myQueue`, and waits till an item becomes available:

```java
@BaseConsumerConfig(queueName = "myQueue", strategy = QueueStrategy.WAIT_ON_AVAILABLE)
public class MyWaitOnAvailableBaseConsumer extends BaseConsumer<Integer> {

    @Override
    public Integer consume(){

        Integer integer;

        if (true){

            //lock the "myQueue" locks in the entire cluster (optional)
            lock();

            try{
                integer = super.consume();
            }finally {
                //releases the "myQueue" locks in the entire cluster
                unlock();
            }
        }

        return integer;
    }
}
```

The consume on demand based is showed as follow:

```java
    BaseConsumer<Integer> baseConsumer = new MyWaitOnAvailableBaseConsumer();
    baseConsumer.consume();
```

### AtomicLock
`AtomicLock` encapsulate Hazelcast ILock. ILock is the distributed implementation of java.util.concurrent.locks.Lock, meaning that if you lock using an ILock,
the critical section that it guards is guaranteed to be executed by only one thread in the entire cluster. Even though locks are great for synchronization,
they can lead to problems if not used properly. So, when called `atomicLock.lock()` means that the lock is acquired and no other thread will run the critical section,
causing a *serial process through the cluster*. `AtomicLock` should be used in `try/catch` section as follow:

```java
    //acquires the lock
    atomicLock.lock();

    try{
        // your critical section here...
    }finally{
        //releases the lock
        atomicLock.unlock();
    }
```

### QueueStrategy
`QueueStrategy` defines the strategy to the client's consumer implementation. There are two strategy definition:

* `ACCEPT_NULL`: Accept `null` element from the queue. This is a non-blocking strategy (calls `pool()` method on the queue).
* `WAIT_ON_AVAILABLE`: Wait on available item from the queue. This is a blocking strategy (calls `take()` method on the queue.

## The API Perspective  
### Workers: *Producer & Consumer*

<p align="center">
        <img alt="Cluster Worker Diagram" src="https://github.com/rs-renato/repository-images/blob/master/cluster-worker/workerProducer_workerConsumer.jpg?raw=true">
</p>

As said, Cluster Worker is a API based on producer x consumer architecture. It uses Hazelcast queue as distributed point for data exchange through the cluster members.
Worker are `runnables` that encapsulate `items`. It comes in two flavors:

#### `WorkerProducer`
`WorkerProducer` is a `runnable` that encapsulate a `ItemProduce` and calls the client's implementation for producing data; This `runnable` will be present in all cluster node, however, will be active at once in an atomic cycle, this means that this `runnable` will die after the production process.

#### `WorkerConsumer`
`WorkerConsumer` is a `runnable` that encapsulate a `ItemProcess` and calls the client's implementation for processing data; These `runnables` are present and active in all cluster nodes, and lives till the cluster member lives.