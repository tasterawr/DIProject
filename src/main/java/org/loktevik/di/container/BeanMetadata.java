package org.loktevik.di.container;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class BeanMetadata {
    private String name;
    private Class<?> clazz;
    private String scope;
    private Object invokeOn;
    private Method method;
    private List<DependencyMetadata> annotationDependencies = new ArrayList<>();
    private ContainerType containerType;
    private BaseDependencyInjectionContainer container;
    private boolean beanCreated = false;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object getInvokeOn() {
        return invokeOn;
    }

    public void setInvokeOn(Object invokeOn) {
        this.invokeOn = invokeOn;
    }

    public ContainerType getContainerType() {
        return containerType;
    }

    public void setContainerType(ContainerType containerType) {
        this.containerType = containerType;
    }

    public List<DependencyMetadata> getAnnotationDependencies() {
        return annotationDependencies;
    }

    public void setAnnotationDependencies(List<DependencyMetadata> annotationDependencies) {
        this.annotationDependencies = annotationDependencies;
    }

    public BaseDependencyInjectionContainer getContainer() {
        return container;
    }

    public void setContainer(BaseDependencyInjectionContainer container) {
        this.container = container;
    }

    public boolean isBeanCreated() {
        return beanCreated;
    }

    public void setBeanCreated(boolean beanCreated) {
        this.beanCreated = beanCreated;
    }
}
