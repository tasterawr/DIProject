package org.loktevik.di.exceptions;

public class ConfigurationClassDrivenContextException extends ContextException{
    public ConfigurationClassDrivenContextException(String s) {
        super(s);
    }

    public ConfigurationClassDrivenContextException(String s, Throwable cause) {
        super(s, cause);
    }
}
