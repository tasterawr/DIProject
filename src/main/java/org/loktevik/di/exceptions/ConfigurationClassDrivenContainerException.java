package org.loktevik.di.exceptions;

public class ConfigurationClassDrivenContainerException extends ContainerException {
    public ConfigurationClassDrivenContainerException(String s) {
        super(s);
    }

    public ConfigurationClassDrivenContainerException(String s, Throwable cause) {
        super(s, cause);
    }
}
