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
public final class HazelcastSupport {

    private static final Logger logger = LogManager.getLogger(HazelcastSupport.class);
    private static final CachedPropertyFile cachedPropertyFile = CachedPropertyFileSupport.getCachedPropertyFile("cw-config");
    
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
    		logger.debug("New hazelcast instance created");
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
        Config hazelcastDefaultConfig = new Config();
        // Configures the log
        hazelcastDefaultConfig.setProperty("hazelcast.logging.type", ClusterWorkerConstants.CW_HAZELCAST_LOGGING_TYPE);
        
        hazelcastDefaultConfig.setInstanceName(hazelcastInstanceName);
        
        // Loads the property configuration values
        int port = cachedPropertyFile.getProperty("cw.network.config.port", Integer.class);
        int portCount = cachedPropertyFile.getProperty("cw.network.config.port.count", Integer.class);
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

        logger.info(String.format("Hazelcast configurations finished: %s", hazelcastDefaultConfig));

        return hazelcastDefaultConfig;
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