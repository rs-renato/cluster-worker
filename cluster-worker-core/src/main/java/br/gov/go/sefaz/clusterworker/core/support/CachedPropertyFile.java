package br.gov.go.sefaz.clusterworker.core.support;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import br.gov.go.sefaz.clusterworker.core.exception.ClusterWorkerException;

/**
 * CachedPropertyFile abstraction class.
 * @author renato-rs
 * @since 1.0
 */
class CachedPropertyFile{

    private static final Logger logger = LogManager.getLogger(CachedPropertyFile.class);

    private static final Map<Class<?>, Method> mapMethod = new HashMap<>();
    private static final Map<String, Object> mapCached = new HashMap<>();

    private final Properties properties;

    /**
     * Constructor of CachedPropertyFile
     * @param properties to be cached
     */
    public CachedPropertyFile(Properties properties) {
        this.properties = properties;
    }

    /**
     * Return a specified property from this {@link CachedPropertyFile}
     * @param propertyName name of the property to be load form property file.
     * @param type of this property to be casted.
     * @return property if it exist.
     */
    public <T> T getProperty(String propertyName, Class<T> type){

        try{
           // Loads from cache if it exist.
           return getCachedProperty(propertyName, type);
        }catch (Exception e){
            logger.error(e);
        }
        return null;
    }

    /**
     * Return a specified property from this {@link CachedPropertyFile}
     * @param propertyName name of the property to be load form property file.
     * @return property if it exist.
     */
    public String getProperty(String propertyName){
        return properties.getProperty(propertyName);
    }

    /**
     * Return a specified property from this {@link CachedPropertyFile}
     * @param propertyName name of the property to be load form property file.
     * @param type of this property to be casted.
     * @return property if it exist.
     * @throws InvocationTargetException 
     * @throws IllegalAccessException 
     * @throws NoSuchMethodException 
     * @throws Exception if cannot to load the property.
     */
    @SuppressWarnings("unchecked")
    private <T> T getCachedProperty(String propertyName, Class<T> type) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException  {

        if(!mapCached.containsKey(propertyName)){

            Method method = getValueOfMethod(type);

            // Invoke the valueOf method.
            String property = properties.getProperty(propertyName);
            
            if (property == null) {
				throw new ClusterWorkerException(String.format("Mandatory property '%s' not defined!", propertyName));
			}
            
			T t = (T) method.invoke(null, property);

            mapCached.put(propertyName, t);
        }
        return (T) mapCached.get(propertyName);
    }

    /**
     * Return a Method from cache.
     * @param type key of this cache.
     * @return method
     * @throws NoSuchMethodException if there is no 'valueOf' method.
     */
    private Method getValueOfMethod(Class<?> type) throws NoSuchMethodException {

        if (!mapMethod.containsKey(type)){
            Method  method = type.getMethod("valueOf", String.class);
            mapMethod.put(type, method);
        }

        return mapMethod.get(type);
    }
}
