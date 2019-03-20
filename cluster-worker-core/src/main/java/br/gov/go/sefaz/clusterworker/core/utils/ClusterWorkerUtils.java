package br.gov.go.sefaz.clusterworker.core.utils;

import java.lang.annotation.Annotation;

import br.gov.go.sefaz.clusterworker.core.exception.MandatoryAnnotationException;

/**
 * Utils to Cluster Worker classes.
 */
public class ClusterWorkerUtils {

    /**
     * Verifies the existence of an annotation on the speficic type.
     * @param type to be verified
     * @param annotationClass to be found
     * @return annotation if there is any
     * @throws MandatoryAnnotationException if there is none
     */
    public static  <T,A extends Annotation> A verifyMandatoryAnotation(T type, Class<A> annotationClass){

        A annotation = type.getClass().getAnnotation(annotationClass);

        if (annotation == null){
            throw new MandatoryAnnotationException(annotationClass.getName() + " annotation expected!");
        }

        return annotation;
    }
}
