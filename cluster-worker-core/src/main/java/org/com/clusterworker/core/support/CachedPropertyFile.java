package org.com.clusterworker.core.support;


import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.com.clusterworker.core.exception.ClusterWorkerException;

import com.hazelcast.util.StringUtil;

/**
 * CachedPropertyFile abstraction class.
 * @author rs-renato
 * @since 1.0.0
 */
public class CachedPropertyFile{

    private static final Logger logger = LogManager.getLogger(CachedPropertyFile.class);

    private static final Map<String, Object> mapCached = new HashMap<>();

    private final Properties properties;

    /**
     * Constructor of CachedPropertyFile
     * @param properties to be cached
     * @since 1.0.0
     */
    public CachedPropertyFile(Properties properties) {
        this.properties = properties;
    }

    /**
     * Return a specified property from this {@link CachedPropertyFile}
     * @param propertyName name of the property to be load form property file.
     * @param type of this property to be casted.
     * @return property if it exist.
     * @since 1.0.0
     */
	public <T> T getProperty(String propertyName, Class<T> type) {

		// Loads from cache if it exist.
		try {
			return getCachedProperty(propertyName, type);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			logger.error("Could not retrieve cached property", e);
		}
		
		return null;
	}
	
	/**
	 * Return a specified property from this {@link CachedPropertyFile} if it exists, otherwise return the default value specifield
	 * @param propertyName name of the property to be load form property file.
     * @param type of this property to be casted.
	 * @param defaultValue to return if property doesn't exists
	 * @return an property value from cached property file or a default value
	 * @since 1.0.0
	 */
	public <T> T getProperty(String propertyName, Class<T> type, T defaultValue) {

		T value = null;
		
		try {
			// Loads from cache if it exist.
			value = getCachedProperty(propertyName, type);
		} catch (Exception e) {
			logger.warn(String.format("Could not retrieve cached property. Reason: %s. The default value '%s' will be returned", e.getMessage(), defaultValue.getClass().isArray() ? Arrays.toString((Object[]) defaultValue) : defaultValue));
		}
		
		return value != null ? value : defaultValue;
	}

	/**
     * Return a specified property from this {@link CachedPropertyFile}
     * @param propertyName name of the property to be load form property file.
     * @param defaultValue to return if property doesn't exists
     * @return property if it exist, otherwise the default value
     * @since 1.0.0
     */
	public String getProperty(String propertyName, String defaultValue){
		String value = getProperty(propertyName);
		if (StringUtil.isNullOrEmpty(value)) {
			logger.warn(String.format("The default value '%s' for property '%s' will be returned", defaultValue, propertyName));
			return defaultValue;
		}
		return value;
	}
	
    /**
     * Return a specified property from this {@link CachedPropertyFile}
     * @param propertyName name of the property to be load form property file.
     * @return property if it exist.
     * @since 1.0.0
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
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    private <T> T getCachedProperty(String propertyName, Class<T> type) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException  {

        if(!mapCached.containsKey(propertyName)){

            // Invoke the valueOf method.
            String property = properties.getProperty(propertyName);
            
            if (StringUtil.isNullOrEmpty(property)) {
				throw new ClusterWorkerException(String.format("Property '%s' not defined!", propertyName));
			}
            
            if (type.isArray()) {
				
            	String[] prop = property.split(",");
            	Class<?> componentType = type.getComponentType();
            	
				T[] propArray = (T[]) Array.newInstance(componentType, prop.length);
            	
            	for (int i = 0; i < prop.length; i++) {
            		Method method = ReflectionSupport.getValueOfMethod(componentType);
            		propArray[i] = (T) method.invoke(null, prop[i].trim());
				}
            	mapCached.put(propertyName, propArray);
			}else {
				Method method = ReflectionSupport.getValueOfMethod(type);
				T t = (T) method.invoke(null, property);
				mapCached.put(propertyName, t);
			}
        }
        return (T) mapCached.get(propertyName);
    }
}
