package org.loktevik.di.exceptions;

public class BeanMetadataHandlerException extends AnnotationDrivenContainerException {

    public BeanMetadataHandlerException(String message){
        super(message);
    }

    public BeanMetadataHandlerException(String message, Throwable cause){
        super(message, cause);
    }
}
