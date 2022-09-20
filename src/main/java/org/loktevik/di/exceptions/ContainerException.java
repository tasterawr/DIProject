package org.loktevik.di.exceptions;

public class ContainerException extends RuntimeException{

    public ContainerException(String s){
        super(s);
    }

    public ContainerException(String s, Throwable cause){
        super(s, cause);
    }
}
