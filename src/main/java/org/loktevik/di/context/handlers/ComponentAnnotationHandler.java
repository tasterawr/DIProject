package org.loktevik.di.context.handlers;

import org.loktevik.di.annotations.Component;
import org.loktevik.di.annotations.MetadataHandler;
import org.loktevik.di.context.BeanMetadata;

import java.util.Locale;

@MetadataHandler
public class ComponentAnnotationHandler implements BeanMetadataHandler{

    @Override
    public void handle(BeanMetadata beanMetadata) {
        Class<?> cl = beanMetadata.getClazz();
        if (!cl.getAnnotation(Component.class).value().equals("")){
            beanMetadata.setName(cl.getAnnotation(Component.class).value());
        } else{
            String firstLetter = cl.getSimpleName().split("")[0].toLowerCase(Locale.ROOT);
            beanMetadata.setName(firstLetter + cl.getSimpleName().substring(1));
        }
    }
}
