package br.gov.go.sefaz.clusterworker.core.support;

import java.util.Set;

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
public final class HazelcastSupport {

    private static final Logger logger = Logger.getLogger(HazelcastSupport.class);
    private static final CachedPropertyFile cachedPropertyFile = CachedPropertyFileSupport.getCachedPropertyFile("cw-config");

    private HazelcastSupport() {
    }

    /**
     * Return a new HazelcastInstance from default configuration 
     * @return hazelcast instance
     */
    public static HazelcastInstance getDefaultHazelcastInstance(){
    	return Hazelcast.getOrCreateHazelcastInstance(getDefaultConfig());
    }
    
    /**
     * Return the hazelcast default configuration.
     * @return hazelcast configuration
     */
    public static Config getDefaultConfig(){

        logger.info("Starting hazelcast default configuration..");

        Config hazelcastConfig = new Config();
        hazelcastConfig.setInstanceName("ClusterWorker");
        
        int port = cachedPropertyFile.getProperty("cw.network.config.port", Integer.class);
        int portCount = cachedPropertyFile.getProperty("cw.network.config.port.count", Integer.class);
        int queueMaxSize = cachedPropertyFile.getProperty("cw.queue.config.max.size", Integer.class);
        boolean multicastEnabled = cachedPropertyFile.getProperty("cw.multicast.config.enabled", Boolean.class);
        String multicastInterface = cachedPropertyFile.getProperty("cw.multicast.config.interface");
//        String queueName = cachedPropertyFile.getProperty("cw.queue.name");

        NetworkConfig networkConfig = hazelcastConfig.getNetworkConfig();
       
        networkConfig.setPort(port)
                .setReuseAddress(true)
                .setPortCount(portCount);

        hazelcastConfig
        	.getQueueConfig(ClusterWorkerConstants.CW_QUEUE_CONFIG_DEFAULT)
//        	.setName(queueName)
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

        logger.info(String.format("Hazelcast configurations finished: %s", hazelcastConfig));

        return hazelcastConfig;
    }

    /**
     * Verifies if there are some hazelcast instance active.
     * @return true is any hazelcast instance was found.
     */
    public static boolean hasHazelcstInstance(){
        Set<HazelcastInstance> hazelcastInstances =  Hazelcast.getAllHazelcastInstances();
        return hazelcastInstances != null && !hazelcastInstances.isEmpty();
    }

    /**
     * Shutdown all entire hazelcast instances if there is any.
     */
    public static void shutdownHazelcast(){

        if (hasHazelcstInstance()){
            logger.warn("Shuttingdown Hazelcast...");
            Hazelcast.shutdownAll();
        }
    }
}