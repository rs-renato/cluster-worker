package br.gov.go.sefaz.clusterworker.core.support;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import br.gov.go.sefaz.clusterworker.core.constants.ClusterWorkerConstants;

/**
 * Hazelcast support class.
 * @author renato-rs
 * @since 1.0
 */
public final class HazelcastDefaultConfigurationSupport {

    private static final Logger logger = LogManager.getLogger(HazelcastDefaultConfigurationSupport.class);
    private static final CachedPropertyFile cachedPropertyFile = CachedPropertyFileSupport.getCachedPropertyFile("cw-config");
    private static final Config hazelcastConfig;

    static {
    	hazelcastConfig = createDefaultConfig();
    }
    
    private HazelcastDefaultConfigurationSupport() {
    }

    /**
     * Return a HazelcastInstance from default configuration. This method grants a running HazelcastInstance
     * @return hazelcast instance
     */
    public static HazelcastInstance getDefaultHazelcastInstance(){
    	
    	HazelcastInstance hazelcastInstance;
    	
    	boolean hasDefaultInstanceRunning = hasActiveDefaultHazelcastInstance();
    	
    	logger.info(String.format("Default HazelcastInstance running: %s", hasDefaultInstanceRunning));

    	// Grants a hazelcast instance running
    	if (!hasDefaultInstanceRunning){
    		shutdownDefaultHazelcastInstance();
        	logger.info("Creating new HazelcastInstance from default configuration");
    		hazelcastInstance =  Hazelcast.newHazelcastInstance(getDefaultConfig());
		}else  {
			logger.info("Retrieving HazelcastInstance from default instance name");
			hazelcastInstance =  Hazelcast.getHazelcastInstanceByName(getDefaultHazelcastInstanceName());
		}
    	
    	return hazelcastInstance;
    }
    
    /**
     * Return the default hazelcas instance name, defined by constant {@link ClusterWorkerConstants#CW_HAZELCAST_INSTANCE_NAME}
     * @return
     */
    public static String getDefaultHazelcastInstanceName() {
    	return ClusterWorkerConstants.CW_HAZELCAST_INSTANCE_NAME;
    }

    /**
     * Verifies if there is the default hazelcast instance
     * @return true is default hazelcast instance was found.
     */
    public static boolean hasActiveDefaultHazelcastInstance(){
        HazelcastInstance hazelcastInstance = Hazelcast.getHazelcastInstanceByName(getDefaultHazelcastInstanceName());
		return hazelcastInstance != null && hazelcastInstance.getLifecycleService().isRunning();
    }

    /**
     * Shutdown all entire hazelcast instances if there is any.
     */
	public static void shutdownDefaultHazelcastInstance(){
        HazelcastInstance hazelcastInstance = Hazelcast.getHazelcastInstanceByName(getDefaultHazelcastInstanceName());
        if (hazelcastInstance != null) {
        	logger.warn("Shuttingdown Default Hazelcast Instance...");
        	hazelcastInstance.shutdown();
        	logger.warn("Default Hazelcast Instance shutdown completed!");
		}
    }
    
    /**
     * Return the hazelcast default configuration.
     * @return hazelcast configuration
     */
    public static Config getDefaultConfig(){
    	return hazelcastConfig;
    }
    
    /**
     * Creates the hazelcast default configuration.
     * @return hazelcast configuration
     */
    private static Config createDefaultConfig(){

        logger.info("Starting hazelcast default configuration..");

        // Creates the default configuration
        Config hazelcastDefaultConfig = new Config();
        // Configures the log
        hazelcastDefaultConfig.setProperty("hazelcast.logging.type", ClusterWorkerConstants.CW_HAZELCAST_LOGGING_TYPE);
        
        hazelcastDefaultConfig.setInstanceName(getDefaultHazelcastInstanceName());
        
        // Loads the property configuration values
        int port = cachedPropertyFile.getProperty("cw.network.config.port", Integer.class);
        int portCount = cachedPropertyFile.getProperty("cw.network.config.port.count", Integer.class);
        int queueMaxSize = cachedPropertyFile.getProperty("cw.queue.config.max.size", Integer.class);
        boolean multicastEnabled = cachedPropertyFile.getProperty("cw.multicast.config.enabled", Boolean.class);
        String multicastInterface = cachedPropertyFile.getProperty("cw.multicast.config.interface");
        
        // Configures network
        NetworkConfig networkConfig = hazelcastDefaultConfig.getNetworkConfig();
       
        networkConfig.setPort(port)
                .setReuseAddress(true)
                .setPortCount(portCount);
        
        JoinConfig join = networkConfig.getJoin();

        join.getMulticastConfig()
                .setEnabled(multicastEnabled)
                .addTrustedInterface(multicastInterface);

        join.getTcpIpConfig()
                .addMember(multicastInterface)
                .setEnabled(true);

        networkConfig.getInterfaces()
                .setEnabled(true)
                .addInterface(multicastInterface);

        // Configures queue
        hazelcastDefaultConfig
        	.getQueueConfig(ClusterWorkerConstants.CW_QUEUE_CONFIG_DEFAULT)
            .setMaxSize(queueMaxSize);
        
        logger.info(String.format("Hazelcast configurations finished: %s", hazelcastDefaultConfig));

        return hazelcastDefaultConfig;
    }
}