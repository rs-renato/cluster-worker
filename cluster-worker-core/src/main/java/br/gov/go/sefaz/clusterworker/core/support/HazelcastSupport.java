package br.gov.go.sefaz.clusterworker.core.support;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.RestApiConfig;
import com.hazelcast.config.RestEndpointGroup;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import br.gov.go.sefaz.clusterworker.core.constants.ClusterWorkerConstants;
import br.gov.go.sefaz.clusterworker.core.support.CachedPropertyFileSupport.SILENT_MODE;

/**
 * Hazelcast support class.
 * @author renato-rs
 * @since 1.0
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
	 */
    public static Config createDefaultConfig(String hazelcastInstanceName){

        logger.debug("Creating hazelcast configuration..");

        // Creates the default configuration
        Config config = new Config(hazelcastInstanceName);
        // Configures the log
        config.setProperty("hazelcast.logging.type", ClusterWorkerConstants.CW_LOGGING_TYPE);
        
        // Loads the property configuration values
        int port = cachedPropertyFile.getProperty("cw.network.port", Integer.class);
        boolean multicastEnabled = cachedPropertyFile.getProperty("cw.network.multicast.enabled", Boolean.class, ClusterWorkerConstants.CW_MULTCAST_ENABLED_DEFAULT);
        String ipMember = cachedPropertyFile.getProperty("cw.network.ip.member", String.class);
        String trustedInterface = cachedPropertyFile.getProperty("cw.network.trusted.interface", ClusterWorkerConstants.CW_NETWORK_TRUSTED_INTERFACE_DEFAULT);
        int maxPoolSize = cachedPropertyFile.getProperty("cw.executor.max.pool.size", Integer.class, ClusterWorkerConstants.CW_EXECUTOR_SERVICE_MAX_POOL_SIZE_DEFAULT);

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
        			.enableGroups(RestEndpointGroup.HEALTH_CHECK,RestEndpointGroup.CLUSTER_READ,RestEndpointGroup.DATA));
        
        // Configure TPC-IP configuration 
        JoinConfig join = network.getJoin();

        join.getMulticastConfig()
        	.setEnabled(multicastEnabled)
        	.addTrustedInterface(trustedInterface);

        join.getTcpIpConfig()
        	.setEnabled(true)
        	.setConnectionTimeoutSeconds(ClusterWorkerConstants.CW_NETWORK_TCP_IP_CONNECTION_TIMEOUT)
        	.addMember(ipMember);

        // Configure Scheduel Executor Service used to schedule Producers
        config.getScheduledExecutorConfig(ClusterWorkerConstants.CW_EXECUTOR_SERVICE_NAME)
        	.setPoolSize(maxPoolSize);

        logger.info(String.format("Hazelcast configurations finished: %s", config));

        return config;
    }
    
    /**
     * Verifies if there is any hazelcast instance running by its name
     * @param hazelcastInstanceName hazelcast instance name
     * @return <code>true<code> if this hazelcast instance is running, <code>false</code> otherwise.
     */
    public static boolean isHazelcastInstanceRunning(String hazelcastInstanceName){
        return isHazelcastInstanceRunning(Hazelcast.getHazelcastInstanceByName(hazelcastInstanceName));
    }
    
    /**
     * Verifies if the given hazelcast instance is running
     * @param hazelcastInstance hazelcast instance
     * @return <code>true<code> if this hazelcast instance is running, <code>false</code> otherwise.
     */
    public static boolean isHazelcastInstanceRunning(HazelcastInstance hazelcastInstance) {
		return hazelcastInstance != null && hazelcastInstance.getLifecycleService().isRunning();
	}
}