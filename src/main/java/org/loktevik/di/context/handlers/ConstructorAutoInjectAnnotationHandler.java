package org.loktevik.di.context.handlers;

import org.loktevik.di.annotations.AutoInject;
import org.loktevik.di.annotations.ByName;
import org.loktevik.di.annotations.MetadataHandler;
import org.loktevik.di.context.BeanMetadata;
import org.loktevik.di.context.DependencyMetadata;
import org.loktevik.di.context.InjectType;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;

@MetadataHandler
public class ConstructorAutoInjectAnnotationHandler implements BeanMetadataHandler{

    @Override
    public void handle(BeanMetadata beanMetadata) {
        Class<?> cl = beanMetadata.getClazz();
        for (Constructor<?> cons : cl.getDeclaredConstructors()){
            if (cons.isAnnotationPresent(AutoInject.class)){
                for (Parameter param : cons.getParameters()){
                    DependencyMetadata dm = new DependencyMetadata();
                    if (param.isAnnotationPresent(ByName.class)){
                        dm.setName(param.getDeclaredAnnotation(ByName.class).value());
                    }
                    dm.setClazz(param.getType());
                    dm.setInjectType(InjectType.CONSTRUCTOR);
                    beanMetadata.getAnnotationDependencies().add(dm);
                }
            }
        }
    }
}
