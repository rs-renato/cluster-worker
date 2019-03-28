package br.gov.go.sefaz.clusterworker.core.support;

import org.apache.log4j.Logger;

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

    private static final Logger logger = Logger.getLogger(HazelcastDefaultConfigurationSupport.class);
    private static final CachedPropertyFile cachedPropertyFile = CachedPropertyFileSupport.getCachedPropertyFile("cw-config");
    private static final Config hazelcastConfig;

    static {
    	hazelcastConfig = createDefaultConfig();
    }
    
    private HazelcastDefaultConfigurationSupport() {
    }

    /**
     * Return a HazelcastInstance from default configuration 
     * @return hazelcast instance
     */
    public static HazelcastInstance getDefaultHazelcastInstance(){
    	return Hazelcast.getOrCreateHazelcastInstance(getDefaultConfig());
    }
    
    /**
     * Creates and return a new HazelcastInstance from default configuration 
     * @return hazelcast instance
     */
    public static HazelcastInstance createDefaultHazelcastInstance(){
    	return Hazelcast.newHazelcastInstance(getDefaultConfig());
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
    public static boolean hasDefaultHazelcastInstance(){
        return Hazelcast.getHazelcastInstanceByName(getDefaultHazelcastInstanceName()) != null;
    }

    /**
     * Shutdown all entire hazelcast instances if there is any.
     */
    public static void shutdownDefaultHazelcastInstance(){
        if (hasDefaultHazelcastInstance()){
            logger.warn("Shuttingdown Default Hazelcast Instance...");
            getDefaultHazelcastInstance().shutdown();
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
        hazelcastDefaultConfig.setInstanceName(getDefaultHazelcastInstanceName());
        
        int port = cachedPropertyFile.getProperty("cw.network.config.port", Integer.class);
        int portCount = cachedPropertyFile.getProperty("cw.network.config.port.count", Integer.class);
        int queueMaxSize = cachedPropertyFile.getProperty("cw.queue.config.max.size", Integer.class);
        boolean multicastEnabled = cachedPropertyFile.getProperty("cw.multicast.config.enabled", Boolean.class);
        String multicastInterface = cachedPropertyFile.getProperty("cw.multicast.config.interface");
        
        NetworkConfig networkConfig = hazelcastDefaultConfig.getNetworkConfig();
       
        networkConfig.setPort(port)
                .setReuseAddress(true)
                .setPortCount(portCount);

        hazelcastDefaultConfig
        	.getQueueConfig(ClusterWorkerConstants.CW_QUEUE_CONFIG_DEFAULT)
            .setMaxSize(queueMaxSize);
        
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
}