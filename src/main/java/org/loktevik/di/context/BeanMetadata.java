package org.loktevik.di.context;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class BeanMetadata {
    private String name;
    private Class<?> clazz;
    private String scope;
    private Object invokeOn;
    private Method method;
    private List<Class<?>> dependencies = new ArrayList<>();
    private List<DependencyMetadata> annotationDependencies = new ArrayList<>();
    private ContextType contextType;

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

    public List<Class<?>> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<Class<?>> dependencies) {
        this.dependencies = dependencies;
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

    public ContextType getContextType() {
        return contextType;
    }

    public void setContextType(ContextType contextType) {
        this.contextType = contextType;
    }

    public List<DependencyMetadata> getAnnotationDependencies() {
        return annotationDependencies;
    }

    public void setAnnotationDependencies(List<DependencyMetadata> annotationDependencies) {
        this.annotationDependencies = annotationDependencies;
    }
}
