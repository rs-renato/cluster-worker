package br.gov.go.sefaz.clusterworker.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Property class utility.
 */
public class PropertyUtils {

    private static final Logger logger = Logger.getLogger(PropertyUtils.class);
    private static final Map<String, Property> propMap = new HashMap<String, Property>();

    /**
     * Load a property file.
     * @param propertyFileName file name of this property.
     * @return property
     */
    public static Property getProperties(String propertyFileName){

        if (!propMap.containsKey(propertyFileName)){

            String extension = ".properties";

            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Properties properties = new Properties();

            try {

                InputStream is = loader.getResourceAsStream(propertyFileName + extension);
                properties.load(is);

                propMap.put(propertyFileName, new Property(properties));

            }catch (IOException e) {
                logger.error("Cannot read resource: " + propertyFileName,e);
            }
        }

        return propMap.get(propertyFileName);
    }
}
