package org.loktevik.di.exceptions;

public class AnnotationDrivenContextException extends ContextException{

    public AnnotationDrivenContextException(String s) {
        super(s);
    }

    public AnnotationDrivenContextException(String s, Throwable cause) {
        super(s, cause);
    }
}
