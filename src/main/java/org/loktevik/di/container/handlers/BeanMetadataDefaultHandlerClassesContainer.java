package org.loktevik.di.container.handlers;

import org.loktevik.di.annotations.*;
import org.loktevik.di.container.BeanMetadata;
import org.loktevik.di.container.DependencyMetadata;
import org.loktevik.di.container.InjectType;
import org.loktevik.di.exceptions.BeanMetadataHandlerException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Locale;

public class BeanMetadataDefaultHandlerClassesContainer {

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

    public class SingletonAnnotationHandler implements BeanMetadataHandler {

        @Override
        public void handle(BeanMetadata beanMetadata) {
            Class<?> cl = beanMetadata.getClazz();
            if (cl.isAnnotationPresent(Singleton.class) && cl.isAnnotationPresent(Prototype.class)){
                throw new BeanMetadataHandlerException(String.format("Class %s is annotated with conflicting scope annotations", cl.getName()));
            } else if (!cl.isAnnotationPresent(Prototype.class)){
                beanMetadata.setScope("singleton");
            }
        }
    }
}
