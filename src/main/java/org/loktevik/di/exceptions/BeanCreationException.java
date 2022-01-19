package org.loktevik.di.exceptions;

public class BeanCreationException extends ContextException{

    public BeanCreationException(String s){
        super(s);
    }

    public BeanCreationException(String s, Throwable cause){
        super(s, cause);
    }
}
