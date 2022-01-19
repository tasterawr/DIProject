package org.loktevik.di.context.handlers;

import org.loktevik.di.annotations.AutoInject;
import org.loktevik.di.annotations.MetadataHandler;
import org.loktevik.di.context.BeanMetadata;
import org.loktevik.di.context.DependencyMetadata;
import org.loktevik.di.context.InjectType;

import java.lang.reflect.Field;

@MetadataHandler
public class FieldAutoInjectAnnotationHandler implements BeanMetadataHandler {

    @Override
    public void handle(BeanMetadata beanMetadata) {
        Class<?> cl = beanMetadata.getClazz();
        for (Field field : cl.getDeclaredFields()) {
            if (field.isAnnotationPresent(AutoInject.class)){
                DependencyMetadata dm = new DependencyMetadata();
                dm.setName(field.getDeclaredAnnotation(AutoInject.class).value());
                dm.setClazz(field.getType());
                dm.setInjectType(InjectType.FIELD);
                beanMetadata.getAnnotationDependencies().add(dm);
            }
        }
    }
}
