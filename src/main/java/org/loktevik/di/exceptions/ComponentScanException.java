package org.loktevik.di.exceptions;

public class ComponentScanException extends AnnotationDrivenContextException{

    public ComponentScanException(String message){
        super(message);
    }

    public ComponentScanException(String message, Throwable cause){
        super(message, cause);
    }
}
