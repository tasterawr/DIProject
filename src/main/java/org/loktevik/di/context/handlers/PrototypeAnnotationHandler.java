package org.loktevik.di.context.handlers;

import org.loktevik.di.annotations.Singleton;
import org.loktevik.di.annotations.Prototype;
import org.loktevik.di.annotations.MetadataHandler;
import org.loktevik.di.context.BeanMetadata;
import org.loktevik.di.exceptions.BeanMetadataHandlerException;

@MetadataHandler
public class PrototypeAnnotationHandler implements BeanMetadataHandler {

    @Override
    public void handle(BeanMetadata beanMetadata) {
        Class<?> cl = beanMetadata.getClazz();
        if (cl.isAnnotationPresent(Singleton.class) && cl.isAnnotationPresent(Prototype.class)){
            throw new BeanMetadataHandlerException(String.format("Class %s is annotated with conflicting scope annotations", cl.getName()));
        } else if (cl.isAnnotationPresent(Prototype.class)){
            beanMetadata.setScope("prototype");
        }
    }
}
