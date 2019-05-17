package br.gov.go.sefaz.clusterworker.core.support;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import br.gov.go.sefaz.clusterworker.core.exception.ClusterWorkerException;
/**
 * CachedPropertyFile support class.
 * @author renato-rs
 * @since 1.0
 */
public class CachedPropertyFileSupport {

    private static final Logger logger = LogManager.getLogger(CachedPropertyFileSupport.class);

    private static final Map<String, CachedPropertyFile> propMap = new HashMap<>();
    
    public enum SILENT_MODE{
    	ENABLED,
    	DISABLED
    }

    private CachedPropertyFileSupport() {
	}
    
    /**
     * Loads a cached property file.
     * @param fileName the cached file name 
     * @return cached property file
     * @throws ClusterWorkerException if some error occurs on file load
     * @since 1.0
     */
    public static CachedPropertyFile getCachedPropertyFile(String fileName){

        if (!propMap.containsKey(fileName)){

            String fileNameWithExtension = fileName + ".properties";

            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Properties properties = new Properties();

            try {
            	// Loads the property file
                InputStream is = loader.getResourceAsStream(fileNameWithExtension);
                properties.load(is);
                // Create the cached property
                propMap.put(fileName, new CachedPropertyFile(properties));

            }catch (Exception e) {
            	throw new ClusterWorkerException(String.format("Could not read the resource '%s'. Did you define in your class path?", fileNameWithExtension));
            }
        }

        return propMap.get(fileName);
    }
    
    /**
     * Loads a cached property file.
     * @param fileName the cached file name 
     * @param silentMode enables or disables the silent mode of loading. if this property is set to <code>true</code> 
     * and there is some error on file load, the errors will be ignored, that is, the property file is not mandatory. 
     * @return a cached property file
     * @since 1.0
     */
    public static CachedPropertyFile getCachedPropertyFile(String fileName, SILENT_MODE silentMode){
			
		try {
			return getCachedPropertyFile(fileName);
		} catch (Exception e) {
			if (SILENT_MODE.ENABLED.equals(silentMode)) {
				logger.warn(String.format("%s. However the silent load is enabled and this error will be ignored!", e.getMessage()));
				return new CachedPropertyFile(new Properties());
			}else {
				throw e;
			}
		}
    }
    
}
