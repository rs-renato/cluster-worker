package br.gov.go.sefaz.clusterworker.core.support;

import java.lang.annotation.Annotation;

import org.apache.log4j.Logger;

import br.gov.go.sefaz.clusterworker.core.exception.MandatoryAnnotationException;

/**
 * Support class for annotation validations
 * @author renato-rs
 * @since 1.0
 */
public class AnnotationSupport {
	
    private static final Logger logger = Logger.getLogger(AnnotationSupport.class);

	private AnnotationSupport() {
	}

    /**
     * Verifies the existence of an annotation on the speficic type.
     * @param type to be verified
     * @param annotationClass to be found
     * @return annotation if there is any
     * @throws MandatoryAnnotationException if the given type hasn't the specified annotation
     */
    public static  <T,A extends Annotation> A assertMandatoryAnnotation(T type, Class<A> annotationClass){
    	logger.debug(String.format("Asserting if annotation '%s' is present on type '%s'", annotationClass, type));
    	
        Class<? extends Object> clazz = type.getClass();
		A annotation = clazz.getAnnotation(annotationClass);

        if (annotation == null){
            throw new MandatoryAnnotationException(String.format("Definition of '%s' annotation is expected on class '%s'!", annotationClass.getName(), clazz.getName()));
        }

        return annotation;
    }
}
