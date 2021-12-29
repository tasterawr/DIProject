package org.loktevik.di.context;

public class DependencyMetadata {
    private String name;
    private Class<?> clazz;
    private InjectType injectType;

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

    public InjectType getInjectType() {
        return injectType;
    }

    public void setInjectType(InjectType injectType) {
        this.injectType = injectType;
    }
}
