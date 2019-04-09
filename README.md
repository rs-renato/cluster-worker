# Cluster Worker - *Scale your task easily.*
**Cluster Worker (CW)** is a Hazelcast based API that help you to scale yours tasks producing and processing  under a cluster environment. CW uses producer x consumer strategy on hazelcast distributed queues as central mechanism to distribute in an easily way the client's task implementations to be executed in all nodes, providing high availability and scalability for processing and exchange data through the cluster members.

> *Hazelcast is an open source In-Memory Data Grid (IMDG). It provides elastically scalable distributed In-Memory computing, widely recognized as the fastest and most scalable approach to application performance. Hazelcast does this in open source and provides highly scalable and available (100% operational, never failing). Distributed applications can use Hazelcast for distributed caching, synchronization, clustering, processing, pub/sub messaging, etc (extracted from  [https://hazelcast.org](https://hazelcast.org))*.

**Table of Content**
  * [From Client Perspective: Producer vs Processor](#from-client-perspective-producer-vs-processor)
    + [Item Producer](#item-producer)
    + [Item Processor](#item-processor)
      + [Consumer Strategy](#consumer-strategy)
    + [Executing Tasks](#executing-tasks)
    + [Standalones: HazelcastQueueProducer & HazelcastQueueConsumer](#standalones-hazelcastqueueproducer-hazelcastqueueconsumer)
      - [HazelcastQueueProducer ](#hazelcastqueueproducer )
      - [HazelcastQueueConsumer](#hazelcastqueueconsumer)
  * [From API Perspective: Producer vs Consumer](#from-api-perspective-producer-vs-consumer)
     - [HazelcastRunnableProducer](#hazelcastrunnableproducer)
     - [HazelcastRunnableConsumer](#hazelcastrunnableconsumer)

## From Client Perspective: Producer vs Processor  

<p align="center">
	<img alt="From Client Perspective: Producer vs Processor" src="https://github.com/rs-renato/repository-images/blob/master/cluster-worker/cluster-worker-client-perspective.png?raw=true">
</p>

Cluster Worker knows how to manage client's tasks, everything you need is provide an implementation for producing and processing data. CW was designed to be task based, and comes in two flavors:

### Item Producer
`ItemProducer's` are tasks managed by `HazelcastRunnableProducer` located into each cluster's node, but **only one will be activated at time, in roundrobin strategy, taking turns between nodes**. This task is responsible to execute a client's implementation  to obtain some items from any source, and CW's internals will put these items on hazelcast distributed queue. It can produce from source as files, web-services, database or whatever kind of source as you need.

>*Note: You can have as many different item producer as you need, but in a cluster environment this task will produce only in one cluster's node to ensure the data won't be produced repeatedly and cause inconsistent processing. If a node fails, there is no problem, this producer is redundant beyond the entire cluster nodes and executed in a roundrobin strategy to grant HA producing and balancing.*

The example bellow shows an  `ItemProducer` implementation that produces a collection of 100 integers to the queue named `cw.example.queue` with execution frequency of 60 seconds:

```java
/**
 * Example of implementation of {@link ItemProducer}
 * @author renato-rs
 * @since 1.0
 */
@ProduceToQueue(queueName = "cw.example.queue", frequency = 60)
public class IntegerItemProducer implements ItemProducer<Integer> {

    @Override
    public Collection<Integer> produce() {

        List<Integer> items = new ArrayList<Integer>();
        
        for (int i = 1; i <= 100; i++){
            items.add(i);
        }
    	// Produces these items to hazelcast distributed queue
        return items;
    }
}
```
        
### Item Processor
`ItemProcessor's` are tasks managed by `HazelcastRunnableConsumer` allocated into each cluster's node (multithread per node). This task is responsible to execute the client's implementation to processing some data read from hazelcast distributed queue. It can process to files (eg. exporting a xml/json), database, web-services, pdf generation, etc.

The example below shows an example of `ItemProcessor` which process one integer obtained from hazelcast distributed queue named `cw.example.queue` with `ConsumerStrategy.WAIT_ON_AVAILABLE` defined to wait an item until it become available (blocking way) and executing with 02 workers (Threads):

>*Note: Into CW internals, **every processor are consumers** from hazelcast queue*.

```java
/**
 * Example of implementation of {@link ItemProcessor}
 * @author renato-rs
 * @since 1.0
 */
@ConsumeFromQueue(queueName = "cw.example.queue", strategy = ConsumerStrategy.WAIT_ON_AVAILABLE, workers = 2)
public class IntegerItemProcessor implements ItemProcessor<Integer> {

	@Override
    public void process(Integer item) {
    	// Process the item obtained from hazelcast distributed queue 
        logger.info("Processing item: " + item);
    }
}
```

### Consumer Strategy
`ConsumerStrategy` defines the distributed queue consumption's strategy . There are two strategy definition:

* `ACCEPT_NULL`: Accepts a `null` element from the queue even if a timeout ocurrs. This is a non-blocking strategy.
* `WAIT_ON_AVAILABLE`: Waits until an element become available from the queue. This is a blocking strategy. 

### Executing Tasks

`ClusterWorker` class is the executor of `ItemProducer` and `ItemProcessor` implementations. An instance of CW which will be handle integers, can be obtained as follow:

```java
// Creates an intance of Cluster Worker to handle integer objects
ClusterWorker<Integer> clusterWorker = ClusterWorkerFactory.getInstance("cw.name").getClusterWorker(Integer.class);

// Executes item producer into cluster
clusterWorker.executeItemProccessor(new IntegerItemProducer());
// Executes item processor into cluster
clusterWorker.executeItemProducer(new IntegerItemProcessor());
```

These tasks of production and processing will handle integer objects through the cluster nodes.

### Standalones: HazelcastQueueProducer & HazelcastQueueConsumer
Cluster Worker allows you have an out of the box approach to control per demand your producing and consumption logic. You can manage when to produce and when to consume data directly to/from hazelcast distributed queue. Everything you need is create an instance of these objects  through `ClusterWorkerFactory`.

<p align="center">
	<img alt="Standalones: HazelcastQueueProducer & HazelcastQueueConsumer" src="https://github.com/rs-renato/repository-images/blob/master/cluster-worker/cluster-worker-out-of-the-box.png?raw=true">
</p>

#### HazelcastQueueProducer
This producer is useful when you need to control your producing process just by calling a `produce()` method. This approach put the data directly into hazelcast distributed queue. The example below shows a `HazelcastQueueProducer` producing integers objects to the queue named `cw.example.queue`:

```java
ArrayList<Integer> items= new ArrayList<Integer>();

// Creates an producer to produces into hazelcast queue
HazelcastQueueProducer<Integer> hazelcastQueueProducer = cwFactory.getHazelcastQueueProducer("cw.example.queue");

for (int i = 0; i < 100; i++) {
	items.add(i);
}
// Produces items on demand
hazelcastQueueProducer.produce(items);
```

#### HazelcastQueueConsumer
This consumer is useful when you need to control your consumption process just by calling a `consume()` method. This approach access the `queue` directly and `take` or `pool` data from it, according with defined `ConsumerStrategy`:
The example below shows a `HazelcastQueueConsumer` which consumes integers objects from hazelcast distibuted queue named `cw.example.queue`, waits until an item becomes available (`ConsumerStrategy.WAIT_ON_AVAILABLE`) and defines the timeout in 02 second:

```java
List<Integer> items = new ArrayList<Integer>;

// Creates an consumer to consumes from hazelcast queue
HazelcastQueueConsumer<Integer> hazelcastQueueConsumer = cwFactory.getHazelcastQueueConsumer("cw.example.queue", ConsumerStrategy.WAIT_ON_AVAILABLE,2);

for (int i = 0; i < 100; i++) {
	items.add(hazelcastQueueConsumer.consume());
}
```

## From API Perspective: Producer vs Consumer

<p align="center">
	<img alt="From API Perspective: Producer vs Consumer" src="https://github.com/rs-renato/repository-images/blob/master/cluster-worker/cluster-worker-api-perspective.png?raw=true">
</p>

As said, Cluster Worker is an API based on `producer vs consumer architecture`. It uses hazelcast distibuted queue to  exchange data through the cluster members. Its internal uses  `Runnable's` that encapsulate the client's implementation of `ItemProducer` and `ItemConsumer`. It comes in two flavors:

### HazelcastRunnableProducer
`WorkerProducer` is a `runnable` that encapsulate a `ItemProduce` and calls the client's implementation for producing data; This `runnable` will be present in all cluster node, however, will be active at once in an atomic cycle, this means that this `runnable` will die after the production process.

### HazelcastRunnableConsumer
`WorkerConsumer` is a `runnable` that encapsulate a `ItemProcess` and calls the client's implementation for processing data; These `runnables` are present and active in all cluster nodes, and lives till the cluster member lives.