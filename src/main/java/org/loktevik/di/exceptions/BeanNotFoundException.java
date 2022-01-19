package org.loktevik.di.exceptions;

public class BeanNotFoundException extends BeanContainerException {

    public BeanNotFoundException(String message){
        super(message);
    }
}
