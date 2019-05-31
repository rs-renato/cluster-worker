package org.com.clusterworker.core.support;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.com.clusterworker.core.constants.ClusterWorkerConstants;
import org.com.clusterworker.core.support.CachedPropertyFileSupport.SILENT_MODE;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.RestApiConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spi.properties.GroupProperty;

/**
 * Hazelcast support class.
 * @author rs-renato
 * @since 1.0.0
 */
public final class HazelcastSupport {

    private static final Logger logger = LogManager.getLogger(HazelcastSupport.class);
    private static final CachedPropertyFile cachedPropertyFile = CachedPropertyFileSupport.getCachedPropertyFile("cw-config", SILENT_MODE.ENABLED);
    
    private HazelcastSupport() {
    }

    /**
     * Gets or creates a new HazelcastInstance from default configuration.
     * @param hazelcastInstanceName  hazelcast instance name
     * @return hazelcastInstance
     * @since 1.0.0
     */
    public static HazelcastInstance getOrcreateDefaultHazelcastInstance(String hazelcastInstanceName){
    	HazelcastInstance hazelcastInstance = Hazelcast.getHazelcastInstanceByName(hazelcastInstanceName); 
    	
		boolean instanceFound = hazelcastInstance != null;
		boolean isRunning = instanceFound && hazelcastInstance.getLifecycleService().isRunning();
		
		logger.debug(String.format("Hazelcast instance found by name '%s': %s - Instance Running: %s", hazelcastInstanceName, instanceFound, isRunning));

		if (!instanceFound || !isRunning) {
    		hazelcastInstance = Hazelcast.newHazelcastInstance(createDefaultConfig(hazelcastInstanceName));
    		logger.debug("New hazelcast instance created..");
		}
    			
		return hazelcastInstance;
    }
    
	/**
	 * Creates the hazelcast default configuration.
	 * @param hazelcastInstanceName hazelcast instance name
	 * @return hazelcast configuration
	 * @since 1.0.0
	 */
    public static Config createDefaultConfig(String hazelcastInstanceName){

        logger.debug("Creating hazelcast configuration..");
        
        // Loads the property configuration values
        int port = cachedPropertyFile.getProperty("cw.network.port", Integer.class);
        String ipMember = cachedPropertyFile.getProperty("cw.network.ip.member");
        
        String trustedInterface = cachedPropertyFile.getProperty("cw.network.trusted.interface", ClusterWorkerConstants.CW_NETWORK_TRUSTED_INTERFACE);
        int timeout = cachedPropertyFile.getProperty("cw.network.connection.timeout", Integer.class, ClusterWorkerConstants.CW_NETWORK_TCP_IP_CONNECTION_TIMEOUT);
        boolean multicastEnabled = cachedPropertyFile.getProperty("cw.network.multicast.enabled", Boolean.class, ClusterWorkerConstants.CW_MULTCAST_ENABLED);
        int maxPoolSize = cachedPropertyFile.getProperty("cw.executor.max.pool.size", Integer.class, ClusterWorkerConstants.CW_EXECUTOR_SERVICE_MAX_POOL_SIZE);
        String[] restApiGroups = cachedPropertyFile.getProperty("cw.rest.api.enable.groups", String[].class, ClusterWorkerConstants.CW_REST_API_GROUPS);

        // Creates the default configuration
        Config config = new Config(hazelcastInstanceName);
        
        // Configures hazelcast configurations for logs and threads
        config.setProperty(GroupProperty.LOGGING_TYPE.getName(), ClusterWorkerConstants.CW_LOGGING_TYPE);
        config.setProperty(GroupProperty.EVENT_THREAD_COUNT.getName(), cachedPropertyFile.getProperty(GroupProperty.EVENT_THREAD_COUNT.getName(), ClusterWorkerConstants.CW_THREAD_COUNT));
        config.setProperty(GroupProperty.IO_THREAD_COUNT.getName(), cachedPropertyFile.getProperty(GroupProperty.IO_THREAD_COUNT.getName(), ClusterWorkerConstants.CW_THREAD_COUNT));
        config.setProperty(GroupProperty.PARTITION_OPERATION_THREAD_COUNT.getName(), cachedPropertyFile.getProperty(GroupProperty.PARTITION_OPERATION_THREAD_COUNT.getName(), ClusterWorkerConstants.CW_THREAD_COUNT));
        config.setProperty(GroupProperty.GENERIC_OPERATION_THREAD_COUNT.getName(), cachedPropertyFile.getProperty(GroupProperty.GENERIC_OPERATION_THREAD_COUNT.getName(), ClusterWorkerConstants.CW_THREAD_COUNT));
        
        // Configures group
        config.getGroupConfig()
    		.setName(hazelcastInstanceName);
        
        // Configures network
        NetworkConfig network = config.getNetworkConfig();
       
        network.setPort(port)
        		.setPortAutoIncrement(true)
        		.setReuseAddress(true)
        	.getInterfaces()
    			.setEnabled(true)
    			.addInterface(trustedInterface);

        // Configures Rest Api
        network.setRestApiConfig(new RestApiConfig()
        	.setEnabled(true)
        	.enableGroups(RestEndpointGroupSupport.converToRestEndpointGroup(restApiGroups)));
        
        // Configure TPC-IP configuration 
        JoinConfig join = network.getJoin();

        join.getMulticastConfig()
        	.setEnabled(multicastEnabled)
        	.addTrustedInterface(trustedInterface);

        join.getTcpIpConfig()
        	.setEnabled(true)
        	.setConnectionTimeoutSeconds(timeout)
        	.addMember(ipMember);

        // Configures Executor Service used to execute producers
        config.getExecutorConfig("default")
            .setPoolSize(maxPoolSize)
            .setStatisticsEnabled(ClusterWorkerConstants.CW_EXECUTOR_SERVICE_STATISTICS_ENABLED)
            .setQueueCapacity(ClusterWorkerConstants.CW_EXECUTOR_SERVICE_MAX_QUEUE_CAPACITY);

        logger.info(String.format("Hazelcast configurations finished: %s", config));

        return config;
    }
    
    /**
     * Verifies if there is any hazelcast instance running by its name
     * @param hazelcastInstanceName hazelcast instance name
     * @return <code>true<code> if this hazelcast instance is running, <code>false</code> otherwise.
     * @since 1.0.0
     */
    public static boolean isHazelcastInstanceRunning(String hazelcastInstanceName){
        return isHazelcastInstanceRunning(Hazelcast.getHazelcastInstanceByName(hazelcastInstanceName));
    }
    
    /**
     * Verifies if the given hazelcast instance is running
     * @param hazelcastInstance hazelcast instance
     * @return <code>true<code> if this hazelcast instance is running, <code>false</code> otherwise.
     * @since 1.0.0
     */
    public static boolean isHazelcastInstanceRunning(HazelcastInstance hazelcastInstance) {
		return hazelcastInstance != null && hazelcastInstance.getLifecycleService().isRunning();
	}
}