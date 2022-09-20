package org.loktevik.di.exceptions;

public class AnnotationDrivenContainerException extends ContainerException {

    public AnnotationDrivenContainerException(String s) {
        super(s);
    }

    public AnnotationDrivenContainerException(String s, Throwable cause) {
        super(s, cause);
    }
}
