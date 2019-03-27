package br.gov.go.sefaz.clusterworker.core.support;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * CachedPropertyFile support class.
 * @author renato-rs
 * @since 1.0
 */
public class CachedPropertyFileSupport {

    private static final Logger logger = Logger.getLogger(CachedPropertyFileSupport.class);
    private static final Map<String, CachedPropertyFile> propMap = new HashMap<>();

    private CachedPropertyFileSupport() {
	}
    
    /**
     * Loads a cached property file.
     * @param fileName file name of this property.
     * @return cached property file
     */
    public static CachedPropertyFile getCachedPropertyFile(String fileName){

        if (!propMap.containsKey(fileName)){

            String extension = ".properties";

            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Properties properties = new Properties();

            try {

                InputStream is = loader.getResourceAsStream(fileName + extension);
                properties.load(is);

                propMap.put(fileName, new CachedPropertyFile(properties));

            }catch (IOException e) {
                logger.error("Cannot read resource: " + fileName,e);
            }
        }

        return propMap.get(fileName);
    }
}
