package org.loktevik.di.container;

public interface DependencyInjectionContainer {
    Object getBean(String beanName);

    <T> T getBean(Class<T> clazz);

    <T> T getBean(String beanName, Class<T> clazz);

    DependencyInjectionContainer run();
}
