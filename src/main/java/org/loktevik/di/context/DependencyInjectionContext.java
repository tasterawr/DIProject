package org.loktevik.di.context;

public interface DependencyInjectionContext {
    Object getBean(String beanName);

    <T> T getBean(Class<T> clazz);

    <T> T getBean(String beanName, Class<T> clazz);

    DependencyInjectionContext run();
}
