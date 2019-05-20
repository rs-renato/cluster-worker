package br.gov.go.sefaz.clusterworker.core.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.config.RestEndpointGroup;

/**
 * RestEndpointGroup support class
 * @author renato.rsilva
 * @since 1.0.0
 */
public class RestEndpointGroupSupport {

    private static final Logger logger = LogManager.getLogger(RestEndpointGroupSupport.class);

    private RestEndpointGroupSupport() {
	}
    
    /**
     * Converts a string array to RestEndpointGroup array, ignoren the unknown properties
     * @param stringRestEndpointGroup to be converted
     * @return RestEndpointGroup array
     * @since 1.0.0
     */
	public static RestEndpointGroup[] converToRestEndpointGroup(String[] stringRestEndpointGroup) {

		Collection<RestEndpointGroup> restEndpointGroup = new ArrayList<>();

		for (int i = 0; i < stringRestEndpointGroup.length; i++) {
			String name = stringRestEndpointGroup[i];
			try {
				restEndpointGroup.add(RestEndpointGroup.valueOf(name));
			} catch (Exception e) {
				logger.error(String.format("Could not convert '%s' to RestEndpointGroup", name));
			}
		}
		
		// Removes null values
		restEndpointGroup.removeAll(Collections.singleton(null));

		return restEndpointGroup.toArray(new RestEndpointGroup[0]);
	}
}
