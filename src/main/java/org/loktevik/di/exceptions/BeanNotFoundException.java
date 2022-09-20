package org.loktevik.di.exceptions;

public class BeanNotFoundException extends BeanRepositoryException {

    public BeanNotFoundException(String message){
        super(message);
    }
}
