package org.com.clusterworker.core.support;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Reflection support class.
 * @author rs-renato
 * @since 1.0.0
 */
public class ReflectionSupport {

	private static final String TYPE_NAME_PREFIX = "class ";
    private static final Map<Class<?>, Method> mapMethod = new HashMap<>();
	
    private ReflectionSupport() {
	}
    
	 /**
     * Return a the 'valueOf' method from cache.
     * @param type key of this cache.
     * @return method
     * @throws NoSuchMethodException if there is no 'valueOf' method.
	 * @throws ClassNotFoundException 
	 * @since 1.0.0
     */
    public static <T> Method getValueOfMethod(Class<T> type) throws NoSuchMethodException {
    	
    	boolean isStringClass = String.class.equals(ReflectionSupport.getClass(type));
    	
        if (!mapMethod.containsKey(type)){
            Method  method = type.getMethod("valueOf", isStringClass ? Object.class : String.class);
            mapMethod.put(type, method);
        }

        return mapMethod.get(type);
    }
    
    /**
     * Return the class name from type
     * @param type
     * @return the class name
     * @since 1.0.0
     */
	public static String getClassName(Type type) {
		if (type == null) {
			return "";
		}
		String className = type.toString();
		if (className.startsWith(TYPE_NAME_PREFIX)) {
			className = className.substring(TYPE_NAME_PREFIX.length());
		}
		return className;
	}

	/**
	 * Return the class from type
	 * @param type
	 * @return the class
	 * @since 1.0.0
	 */
	public static Class<?> getClass(Type type){
		
		Class<?> clazz = null;
		
		try {
			return Class.forName(getClassName(type));
		} catch (ClassNotFoundException e) {
			return clazz;
		}
	}
}
