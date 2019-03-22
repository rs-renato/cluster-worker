package br.gov.go.sefaz.clusterworker.core.support;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Property class utility.
 */
public class PropertySupport {

    private static final Logger logger = Logger.getLogger(PropertySupport.class);
    private static final Map<String, PropertyFile> propMap = new HashMap<String, PropertyFile>();

    /**
     * Load a property file.
     * @param fileName file name of this property.
     * @return property
     */
    public static PropertyFile getPropertyFile(String fileName){

        if (!propMap.containsKey(fileName)){

            String extension = ".properties";

            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Properties properties = new Properties();

            try {

                InputStream is = loader.getResourceAsStream(fileName + extension);
                properties.load(is);

                propMap.put(fileName, new PropertyFile(properties));

            }catch (IOException e) {
                logger.error("Cannot read resource: " + fileName,e);
            }
        }

        return propMap.get(fileName);
    }
}
