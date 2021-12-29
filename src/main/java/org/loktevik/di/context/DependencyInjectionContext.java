package org.loktevik.di.context;

public interface DependencyInjectionContext {
    Object getBean(String beanName);

    Object getBean(Class<?> clazz);
}
