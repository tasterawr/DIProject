package org.loktevik.di.exceptions;

public class BeanNotFoundException extends BeanFactoryException{

    public BeanNotFoundException(String message){
        super(message);
    }
}
