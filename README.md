# Cluster Worker - *Scalable Batch Application Easily.*
**Cluster Worker (CW)** is a Hazelcast java based API that helps to scale traditional batch application
just applying separation of concerns in producing and processing tasks. CW uses *producer vs consumer* strategy on hazelcast distributed queues as central mechanism to distribute work under a cluster environment, providing high availability and vertical & horizontal scalability for processing and exchange data through the cluster members.
> *Hazelcast is an open source In-Memory Data Grid (IMDG). It provides elastically scalable distributed In-Memory computing, widely recognized as the fastest and most scalable approach to application performance. Hazelcast does this in open source and provides highly scalable and available (100% operational, never failing). 
> [https://hazelcast.org](https://hazelcast.org)*

**Table of Content**
  * [Which Problem Cluster Worker intend to solve?](#which-problem-cluster-worker-intend-to-solve)
  * [From Client Perspective: Producer vs Processor](#from-client-perspective-producer-vs-processor)
    + [Item Producer](#item-producer)
    + [Item Processor](#item-processor)
      + [Consumer Strategy](#consumer-strategy)
    + [Executing Tasks](#executing-tasks)
    + [Standalones: HazelcastQueueProducer & HazelcastQueueConsumer](#standalones-hazelcastqueueproducer-hazelcastqueueconsumer)
      - [HazelcastQueueProducer ](#hazelcastqueueproducer )
      - [HazelcastQueueConsumer](#hazelcastqueueconsumer)
      - [Hybrid Usage: Spring Batch & Cluster Worker](#hybrid-usage-spring-batch-cluster-worker)
  * [From API Perspective: Producer vs Consumer](#from-api-perspective-producer-vs-consumer)
     - [HazelcastCallableProducer](#hazelcastcallableproducer)
     - [HazelcastCallableConsumer](#hazelcastcallableconsumer)
  * [Configurations](#configurations)
      - [Default CW Configurations](#default-cw-configurations)
      - [Default Hazelcast Configurations](#default-hazelcast-configurations)
  * [External Links](#external-links)

## Which Problem Cluster Worker intend to solve? 
Traditional batch application --*these which initially was designed without cares about scalability, untill it really needs*-- usually lacks on separation of concerns on *reads & writes* operations, which may cause innumerous inconsistencies on processing data flow, if that application have been deployed as clustered application. That's because many read operations will be executed at same time on different JVM, reading and processing the same data set, causing re-work between nodes and data inconsistencies. 

Usually this kind of batch application is deployed as ACTIVE-PASSIVE (failover or round-robin tournament strategy) and just grants high disponibility, where only one node is activated at time. In that case, this application won't cover scalability because of concurrency in distributed environment, which in that case, prevents a cluster deployment.

<p align="center">
	<img alt="ClusterWorker-Deployment Diagram [Scalable]" src="https://raw.githubusercontent.com/rs-renato/repository-images/master/cluster-worker/ClusterWorker-Deployment%20Diagram%20%5BNon-Scalable%5D.png">
</p>

That way, CW comes to cover this lack, and its an option for non-scalable batch application which needs to scale-up or scale-out without too much effort for re-design. The only thing is need is the separation of concerns between read operation (obtain the data set to be processed) and write operation (the processing itself).

## From Client Perspective: Producer vs Processor 

Cluster Worker knows how to manage client’s tasks, everything that is need is providing an implementation for producing and processing data. These tasks will be distributed and executed through entire cluster.
The main work that CW does is orchestrate the process of reads defined by client's implementation of `ItemProducer`. After reads the data, CW produces these data into *Hazelcast Distributed Queue*, on the other hand, `ItemProcessor's` process their work in cluster-wide concurrent fashion. Simple like that!

<p align="center">
	<img alt="ClusterWorker-Deployment Diagram [Scalable]" src="https://raw.githubusercontent.com/rs-renato/repository-images/master/cluster-worker/ClusterWorker-Deployment%20Diagram%20%5BScalable%5D.png">
</p>

### Item Producer
`ItemProducer's` are tasks managed by `HazelcastCallableProducer` located into each cluster's node, but **only one will be executed at time, in round-robin strategy, taking turns between nodes**. This task is responsible to execute a client's implementation to obtain items from any source, and CW's internals will put these items into hazelcast distributed queue. It can produce from sources as files, web-services, database or whatever kind of source that's need.

>*Note: It's possible to have as much different implementation of item producer as need, but in a cluster environment this task will produce only in one cluster's node to ensure the data won't be produced repeatedly and cause inconsistent processing. If a node fails, there is no problem; this producer is redundant beyond the entire cluster nodes and executed in a round-robin strategy to grant HA producing and balancing.*

The example bellow shows an `ItemProducer` implementation that produces a collection of 100 integers to the queue named `cw.example.queue`, with frequency of 30 seconds and set the max queue size to 100 items:

```java
/**
 * Example of implementation of {@link ItemProducer}
 * @author rs-renato
 * @since 1.0.0
 */
@ProduceToQueue(queueName = "cw.example.queue", cronExpression= "0/30 * * * * ?", maxSize = 100)
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
>*Note¹: The cron expression support is implemented using [Quartz Scheduler](http://www.quartz-scheduler.org/documentation/)*. 

### Item Processor
`ItemProcessor's` are tasks managed by `HazelcastCallableConsumer` allocated into each cluster's node (multithread per node). This task is responsible to execute the client's implementation to processing some data read from hazelcast distributed queue. It can process to files (eg. exporting a xml/json), database, web-services, pdf generation, or whatever destination aligned with business needs.

The example below shows an example of `ItemProcessor` which process one integer obtained from hazelcast distributed queue named `cw.example.queue` with `ConsumerStrategy.WAIT_ON_AVAILABLE`, which configures to wait an item until it become available (blocking way) and executing with 02 workers (threads):

```java
/**
 * Example of implementation of {@link ItemProcessor}
 * @author rs-renato
 * @since 1.0.0
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
>*Note¹: Into CW internals (API Perspective), **every processor are consumers** from hazelcast queue*.
>*Note²: Item processors could also be configured with non-blocking strategy, defining an timeout to wait avaliable items to be processed, **before return null to ItemProcessor.process(item)** invocation.*

### Consumer Strategy
`ConsumerStrategy` defines the distributed queue consumption's strategy. There are two strategy definitions:

* `ACCEPT_NULL`: Accept an `null` element from the queue even if a timeout ocurrs. This is a non-blocking strategy.
* `WAIT_ON_AVAILABLE`: Wait until an element become available from the queue. This is a blocking strategy. 

### Executing Tasks

`ClusterWorker` class is the executor of `ItemProducer` and `ItemProcessor` implementations. An instance of CW which will be handle integers, can be obtained as follow:

```java
// Creates a ClusterWorkerFactory instance. This invocation creates an internal hazelcast instance named 'cw.name' with CW's default configurations
ClusterWorkerFactory cwFactory = ClusterWorkerFactory.getInstance("cw.name");
// Creates an intance of Cluster Worker to handle integer objects
ClusterWorker<Integer> clusterWorker = cwFactory.getClusterWorker(Integer.class);

// Executes item producer into cluster
clusterWorker.executeItemProcessor(new IntegerItemProducer());
// Executes item processor into cluster
clusterWorker.executeItemProducer(new IntegerItemProcessor());

// Shutdown the clusterWorker and its threads (producer and consumers)
// This method shutdown the factory internal hazelcast instance, because that instance was created by this factory.
cwFactory.shutdown(clusterWorker);
```
>*Note: For custom hazelcast instance, cwFactory could be defined as follow:*
>```java
>HazelcastInstance hzInstance = ...;
>cwFactory = ClusterWorkerFactory.getInstance(hzInstance);
>```

### Standalones: HazelcastQueueProducer & HazelcastQueueConsumer
Cluster Worker allows an out of the box approach to control producing and consumption logic per demand. It's possible to manage when to produce and when to consume data directly to/from hazelcast distributed queue. Everything that is need is to create an instance of these objects through `ClusterWorkerFactory`.

#### HazelcastQueueProducer
This producer is useful when needs to control the producing process just by calling a `produce()` method. This approach put the data directly into hazelcast distributed queue. The example below shows a `HazelcastQueueProducer` producing integers objects to the queue named `cw.example.queue`:

```java
// Creates an ClusterWorkerFactory instance. This invocation creates an internal hazelcast instance named 'cw.name' with CW's default configurations
ClusterWorkerFactory cwFactory = ClusterWorkerFactory.getInstance("cw.name");
// Creates a producer to produce into hazelcast queue
HazelcastQueueProducer<Integer> hazelcastQueueProducer = cwFactory.getHazelcastQueueProducer("cw.example.queue");

// Produces items on demand
ArrayList<Integer> items = readsFromSource();
hazelcastQueueProducer.produce(items);
```

#### HazelcastQueueConsumer
This consumer is useful when its need to control the consumption process just by calling a `consume()` method. This approach access the `queue` directly and `take` or `pool` data from it, according with defined `ConsumerStrategy`:
The example below shows a `HazelcastQueueConsumer` which consumes integers objects from hazelcast distributed queue named `cw.example.queue`, waits until an item becomes available (`ConsumerStrategy.WAIT_ON_AVAILABLE`) and defines the timeout in 02 second:

```java
// Creates an ClusterWorkerFactory instance. This invocation creates an internal hazelcast instance named 'cw.name' with CW's default configurations
ClusterWorkerFactory cwFactory = ClusterWorkerFactory.getInstance("cw.name");
// Creates a consumer to consume from hazelcast queue
HazelcastQueueConsumer<Integer> hazelcastQueueConsumer = cwFactory.getHazelcastQueueConsumer("cw.example.queue", ConsumerStrategy.WAIT_ON_AVAILABLE,2);

// Consumes items on demand
List<Integer> items = new ArrayList<Integer>;
for (int i = 0; i < 100; i++) {
	items.add(hazelcastQueueConsumer.consume());
}
```
#### Hybrid Usage: Spring Batch & Cluster Worker
Its also possible use Cluster Worker in a hybrid way, For example: letting CW manages the scheduled production (responsability of reads data from the source in a defined frequency), and letting Spring Batch ItemReader consumes from distributed queue. The configuration bellow shows how to handle with this use case: 

*Cluster Worker Configuration:*

```java
/**
 * Example of Cluster Worker Spring Configuration
 * @author rs-renato
 * @since 1.0.0
 */
@Configuration
public class ClusterWorkerConfiguration {	
	
	@Bean
	public ClusterWorkerFactory cwFactory(){
		return ClusterWorkerFactory.getInstance("cw.name");
	}
	
	@Bean
	public HazelcastQueueConsumer<Integer> hazelcastQueueConsumer(ClusterWorkerFactory cwFactory){
		return cwFactory.getHazelcastQueueConsumer("cw.example.queue");
	}
	
	@Bean
	public IntegerItemProducer integerItemProducer() {
		return new IntegerItemProducer();
	}
}
```

*Spring ItemReader:*

```java
/**
 * Example of Cluster Worker Spring ItemReader
 * @author rs-renato
 * @since 1.0.0
 */
@Component
public class ClusterWorkerItemReader implements ItemReader<Integer>{

	@Autowired
	private HazelcastQueueConsumer<Integer> hazelcastQueueConsumer;
	
	@Override
	public Integer read() throws Exception {
		return hazelcastQueueConsumer.consume();
	}
}
```

*On startup application:*

```java
@Autowired
private ClusterWorkerFactory cwFactory;

@Autowired
private HazelcastItemProducer hazelcastItemProducer;
...

ClusterWorker clusterWorker = cwFactory.getClusterWorker(Integer.class);
try {
	// Executes item producer
	clusterWorker.executeItemProducer(hazelcastItemProducer);
} catch (ItemProducerException e) {
	// Releases the resources
	cwFactory.shutdown(clusterWorker);
}			
```

## From API Perspective: Producer vs Consumer
As said, Cluster Worker is an API based on `producer vs consumer architecture`. It uses hazelcast distributed queue to exchange data through the cluster members. It's internals uses `Callable's` that encapsulate the client's implementation of `ItemProducer` and `ItemProcessor`. 

<p align="center">
	<img alt="From Client Perspective: Producer vs Processor" src="https://raw.githubusercontent.com/rs-renato/repository-images/master/cluster-worker/ClusterWorker-Component%20Diagram.png">
</p>

### HazelcastCallableProducer
`HazelcastCallableProducer` is a callable that encapsulate a `ItemProducer` and calls the client's implementation for producing data. This callable will be present in all cluster nodes, however, it will be executed as atomic cycle, that means, the callable's thread will die after the production process.

>*Note: CW has a global executor service to handle all production tasks*.

### HazelcastCallableConsumer
`HazelcastCallableConsumer` is a callable that encapsulate a `ItemProcessor` and calls the client's implementation for processing data; these callables are present and active in all cluster nodes, and lives till the cluster member lives, that is, they are long living running task. 

>*Note: CW has a distributed executor service associated to each implementation of `ItemProcessor`. The thread pool of this executor service is bounded by configuration `@ConsumeFromQueue(workers=X)` where X is the max-pool size which that executor service can handle. That way, all threads responsible to executes a respective processing task, will be managed by a specific thread pool.*

## Configurations
### Default CW Configurations
Cw defines a file `cw-config.properties` with the following properties:

```properties
# The port which Hazelcast member will try to bind on
cw.network.port=

# The IP member (well-known member) to add to the cluster. Could be comma separated.
cw.network.ip.member=

# The default trusted interface (default is 127.0.0.1).
cw.network.trusted.interface=

# The maximum amount of time Hazelcast will try to connect to a well-known member before giving up (default is 10).
cw.network.connection.timeout=

# Enables or disables the multicast discovery mechanism (default is false).
cw.network.multicast.enabled=

# The number of executor threads used for producers threads (default is 2).
cw.executor.max.pool.size=

# The default REST API groups enabled. Could be comma separated (default is HEALTH_CHECK, CLUSTER_WRITE, CLUSTER_READ, DATA).
cw.rest.api.enable.groups=
```
>*Note: The properties `cw.network.port` and `cw.network.ip.member` are mandatory*.

### Default Hazelcast Configurations
For opmization, CW defines the following hazelcast system configurations:

```properties
# Number of event handler threads (default is 2)
hazelcast.event.thread.count=

# Number of threads performing socket input and socket output (default is 2).
hazelcast.io.thread.count=

# Number of partition based operation handler threads (default is 2). -1 means CPU core count.
hazelcast.operation.thread.count=

# Number of generic operation handler threads (default is 2). -1 means CPU core count / 2.
hazelcast.operation.generic.thread.count=
```
>*Note: If these configurations doesn't meet the needs, nothing prevents to configure the HazelcastInstance as wished.*

## External Links
The major dependencies that Cluster Worker depends is Hazelcast and Quartz:

 - [https://hazelcast.org/documentation/](https://hazelcast.org/documentation/)
 - [https://www.quartz-scheduler.org/documentation/](https://www.quartz-scheduler.org/documentation/)