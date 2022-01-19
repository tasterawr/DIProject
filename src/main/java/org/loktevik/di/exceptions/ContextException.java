package org.loktevik.di.exceptions;

public class ContextException extends RuntimeException{

    public ContextException(String s){
        super(s);
    }

    public ContextException(String s, Throwable cause){
        super(s, cause);
    }
}
