package br.gov.go.sefaz.clusterworker.core.utils;

import java.util.Set;

import org.apache.log4j.Logger;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

/**
 * Hazelcast utility class configuration.
 */
public final class HazelcastUtils {

    private static final Logger logger = Logger.getLogger(HazelcastUtils.class);
    private static final Property property = PropertyUtils.getProperties("cw-config");

    private static final HazelcastUtils instance = new HazelcastUtils();
    private static HazelcastInstance hazelcastInstance;

    private HazelcastUtils() {
        hazelcastInstance = Hazelcast.newHazelcastInstance(getConfig());
    }

    public static HazelcastUtils getInstance() {
        return instance;
    }

    public synchronized HazelcastInstance getHazelcastInstance(){

        if (!hazelcastInstance.getLifecycleService().isRunning()){
            hazelcastInstance = Hazelcast.newHazelcastInstance(getConfig());
        }
        return hazelcastInstance;
    }

    /**
     * Return a hazelcast configuration.
     * @return
     */
    private Config getConfig(){

        logger.info("Starting hazelcast configurations..");

        Config config = new Config();
        config.setInstanceName("ClusterWorker");

        int port = property.getProperty("cw.network.config.port", Integer.class);
        int portCount = property.getProperty("cw.network.config.port.count", Integer.class);
//        int queueMaxSizwe = property.getProperty("cw.queue.config.max.size", Integer.class);
        boolean multicastEnabled = property.getProperty("cw.multicast.config.enabled", Boolean.class);
        String multicastInterface = property.getProperty("cw.multicast.config.interface");

        NetworkConfig networkConfig = config.getNetworkConfig();

        networkConfig.setPort(port)
                .setReuseAddress(true)
                .setPortCount(portCount);

//        config.getQueueConfig(property.getProperty("cw.queue.working.task"))
//                .setMaxSize(queueMaxSizwe);

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

        logger.info("Hazelcast configurations finished!");

        return config;
    }

    /**
     * Verifies if there are some hazelcast instance active.
     * @return true is any hazelcast instance was found.
     */
    private boolean hasHazelcstInstance(){
        Set<HazelcastInstance> hazelcastInstances =  Hazelcast.getAllHazelcastInstances();
        return hazelcastInstances != null && !hazelcastInstances.isEmpty();
    }

    /**
     * Shutdown all entire hazelcast instances if there is any.
     */
    public void shutdownHazelcast(){

        if (hasHazelcstInstance()){
            logger.warn("Shuttingdown Hazelcast...");
            Hazelcast.shutdownAll();
        }
    }
}