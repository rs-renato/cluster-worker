package br.gov.go.sefaz.clusterworker.core.support;

import java.lang.annotation.Annotation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import br.gov.go.sefaz.clusterworker.core.exception.MandatoryAnnotationException;

/**
 * Support class for annotation validations
 * @author renato.rsilva
 * @since 1.0.0
 */
public class AnnotationSupport {
	
    private static final Logger logger = LogManager.getLogger(AnnotationSupport.class);

	private AnnotationSupport() {
	}

    /**
     * Verifies the existence of an annotation on the speficic type.
     * @param type to be verified
     * @param annotationClass to be found
     * @return annotation if there is any
     * @throws MandatoryAnnotationException if the given type hasn't the specified annotation
     * @since 1.0.0
     */
    public static  <T,A extends Annotation> A assertMandatoryAnnotation(T type, Class<A> annotationClass){
    	Class<? extends Object> clazz = type.getClass();

    	String className = clazz.getName();
		logger.trace(String.format("Asserting if annotation '%s' is present on type '%s'", annotationClass, className));
    	
		A annotation = clazz.getAnnotation(annotationClass);

        if (annotation == null){
            throw new MandatoryAnnotationException(String.format("Definition of '%s' annotation is expected on class '%s'!", annotationClass.getName(), className));
        }

        return annotation;
    }
}
