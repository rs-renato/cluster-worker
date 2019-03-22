package br.gov.go.sefaz.clusterworker.core.support;

import java.lang.annotation.Annotation;

import br.gov.go.sefaz.clusterworker.core.exception.MandatoryAnnotationException;

/**
 * Utils to Cluster Worker classes.
 */
public class AnnotationSupport {
	
	private AnnotationSupport() {
	}

    /**
     * Verifies the existence of an annotation on the speficic type.
     * @param type to be verified
     * @param annotationClass to be found
     * @return annotation if there is any
     * @throws MandatoryAnnotationException if there is none
     */
    public static  <T,A extends Annotation> A assertMandatoryAnotation(T type, Class<A> annotationClass){

        Class<? extends Object> clazz = type.getClass();
		A annotation = clazz.getAnnotation(annotationClass);

        if (annotation == null){
            throw new MandatoryAnnotationException(String.format("Definition of '%s' annotation is expected on class '%s'!", annotationClass.getName(), clazz.getName()));
        }

        return annotation;
    }
}
