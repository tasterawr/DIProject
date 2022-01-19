package org.loktevik.di.context.handlers;

import org.loktevik.di.annotations.AutoInject;
import org.loktevik.di.annotations.ByName;
import org.loktevik.di.annotations.MetadataHandler;
import org.loktevik.di.context.BeanMetadata;
import org.loktevik.di.context.DependencyMetadata;
import org.loktevik.di.context.InjectType;

import java.lang.reflect.Method;

@MetadataHandler
public class SetterAutoInjectAnnotationHandler implements BeanMetadataHandler{

    @Override
    public void handle(BeanMetadata beanMetadata) {
        Class<?> cl = beanMetadata.getClazz();
        for (Method m : cl.getDeclaredMethods()){
            if (m.isAnnotationPresent(AutoInject.class) && m.getParameters().length == 1 && m.getName().indexOf("set") == 0){
                DependencyMetadata dm = new DependencyMetadata();
                if (m.getParameters()[0].isAnnotationPresent(ByName.class)){
                    dm.setName(m.getParameters()[0].getDeclaredAnnotation(ByName.class).value());
                } else{
                    dm.setName(m.getDeclaredAnnotation(AutoInject.class).value());

                }
                dm.setClazz(m.getParameterTypes()[0]);
                dm.setInjectType(InjectType.SETTER);
                beanMetadata.getAnnotationDependencies().add(dm);
            }
        }
    }
}
