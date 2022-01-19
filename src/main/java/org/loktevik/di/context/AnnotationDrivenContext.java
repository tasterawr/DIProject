package org.loktevik.di.context;

import org.loktevik.di.annotations.*;
import org.loktevik.di.context.handlers.BeanMetadataHandler;
import org.loktevik.di.exceptions.BeanCreationException;
import org.loktevik.di.exceptions.BeanNotFoundException;
import org.loktevik.di.exceptions.ComponentScanException;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AnnotationDrivenContext extends BaseDependencyInjectionContext implements DependencyInjectionContext {
    private final List<Class<?>> classes;
    private final String basePackageName;
    private final List<BeanMetadataHandler> handlers;
    private ConfigurationClassDrivenContext configurationClassDrivenContext = null;

    {
        classes = new ArrayList<>();
        basePackageName = this.getClass().getName().split("\\.")[0];
        handlers = new ArrayList<>();
    }

    public AnnotationDrivenContext(Path componentScanPath){
        scanComponents(componentScanPath);
        getBeanMetadataHandlers();
        List<Class<?>> componentClasses = getComponentClasses();

        for (Class<?> cl : componentClasses){
            BeanMetadata beanMetadata = new BeanMetadata();
            beanMetadata.setClazz(cl);
            beanMetadata.setContextType(ContextType.ANNOTATION_DRIVEN);
            beanMetadata.setContext(this);
            beanMetadata.setAnnotationDependencies(new ArrayList<>());

            for (BeanMetadataHandler handler : handlers){
                handler.handle(beanMetadata);
            }
            MainBeanContainer.addBeanMetadata(beanMetadata);
        }

        List<Class<?>> includedConfigurations = getIncludedConfigurations();
        if (includedConfigurations.size() != 0){
            configurationClassDrivenContext = new ConfigurationClassDrivenContext(includedConfigurations.toArray(new Class<?>[0]));
        }
    }

    private List<Class<?>> getIncludedConfigurations() {
        return classes.stream()
                .filter(clazz -> clazz.isAnnotationPresent(Configuration.class) && clazz.isAnnotationPresent(Included.class))
                .collect(Collectors.toList());
    }

    private List<Class<?>> getComponentClasses() {
        return classes.stream()
                .filter(clazz -> clazz.isAnnotationPresent(Component.class))
                .collect(Collectors.toList());
    }

    private void getBeanMetadataHandlers() {
        classes.stream()
                .filter(clazz -> clazz.isAnnotationPresent(MetadataHandler.class))
                .forEach(clazz -> {
                    try {
                        BeanMetadataHandler handler = (BeanMetadataHandler)clazz.getDeclaredConstructor().newInstance();
                        handlers.add(handler);
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        throw new ComponentScanException(String.format("Could not create instance of class %s.", clazz), e);
                    }
                });
    }

    @Override
    protected void createBeanFromMetadata(BeanMetadata beanMetadata) {
        try {
            if (beanMetadata.getScope().equals("singleton")){
                Object bean;
                if (beanMetadata.getAnnotationDependencies().size() == 0){
                    bean = beanMetadata.getClazz().getDeclaredConstructor().newInstance();
                } else {
                    bean = createBeanWithDependencies(beanMetadata);
                }
                MainBeanContainer.addSingletonBean(beanMetadata.getName(), bean);
            } else if (beanMetadata.getScope().equals("prototype")){
                MainBeanContainer.addPrototypeBean(beanMetadata.getName(), beanMetadata);
            }
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException | InstantiationException e) {
            throw new BeanCreationException(String.format("Could not create bean for class %s", beanMetadata.getClazz()), e);
        }
    }

    private Object createBeanWithDependencies(BeanMetadata beanMetadata) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        List<Object> fieldDependencyBeans = new ArrayList<>();
        List<Object> consDependencyBeans = new ArrayList<>();
        List<Object> setterDependencyBeans = new ArrayList<>();
        getAllDependencies(beanMetadata, fieldDependencyBeans, consDependencyBeans, setterDependencyBeans);

        Object bean = injectConstructorDependencies(beanMetadata, consDependencyBeans);
        injectFieldDependencies(beanMetadata, fieldDependencyBeans, bean);
        injectSetterDependencies(beanMetadata, setterDependencyBeans, bean);
        return bean;
    }

    private void getAllDependencies(BeanMetadata beanMetadata, List<Object> fieldDependencyBeans, List<Object> consDependencyBeans, List<Object> setterDependencyBeans) {
        for (DependencyMetadata dm : beanMetadata.getAnnotationDependencies()){
            Object dependencyBean = getDependencyBean(dm);

            if (dependencyBean == null && !"".equals(dm.getName()) && dm.getName() != null){
                throw new BeanNotFoundException(String.format("Bean with name \"%s\" not found", dm.getName()));
            } else if (dependencyBean == null){
                throw new BeanNotFoundException(String.format("Bean with class \"%s\" not found", dm.getClazz()));
            }

            if (dm.getInjectType().equals(InjectType.FIELD)){
                fieldDependencyBeans.add(dependencyBean);
            } else if (dm.getInjectType().equals(InjectType.CONSTRUCTOR)){
                consDependencyBeans.add(dependencyBean);
            } else {
                setterDependencyBeans.add(dependencyBean);
            }
        }
    }

    private void injectSetterDependencies(BeanMetadata beanMetadata, List<Object> setterDependencyBeans, Object bean) throws IllegalAccessException, InvocationTargetException {
        if (setterDependencyBeans.size() != 0){
            for (Method m: beanMetadata.getClazz().getDeclaredMethods()){
                if (m.isAnnotationPresent(AutoInject.class)){
                    m.setAccessible(true);
                    Object dependency = setterDependencyBeans.stream()
                            .filter(depBean -> depBean.getClass().equals(m.getParameterTypes()[0]))
                            .findFirst().orElse(null);
                    m.invoke(bean, dependency);
                }
            }
        }
    }

    private void injectFieldDependencies(BeanMetadata beanMetadata, List<Object> fieldDependencyBeans, Object bean) throws IllegalAccessException {
        if (fieldDependencyBeans.size() != 0){
            for (Field field : beanMetadata.getClazz().getDeclaredFields()){
                if (field.isAnnotationPresent(AutoInject.class)){
                    field.setAccessible(true);
                    Object dependency = fieldDependencyBeans.stream()
                            .filter(depBean -> depBean.getClass().equals(field.getType()))
                            .findFirst().orElse(null);
                    field.set(bean, dependency);
                }
            }
        }
    }

    private Object injectConstructorDependencies(BeanMetadata beanMetadata, List<Object> consDependencyBeans) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Object bean;
        if (consDependencyBeans.size() != 0){
            Constructor<?> declaredConstructor = beanMetadata.getClazz().getDeclaredConstructor(beanMetadata.getAnnotationDependencies().stream()
                    .filter(dm -> dm.getInjectType().equals(InjectType.CONSTRUCTOR))
                    .map(DependencyMetadata::getClazz).toArray(Class[]::new));

            bean = declaredConstructor.newInstance(consDependencyBeans.toArray());
        } else {
            bean = beanMetadata.getClazz().getDeclaredConstructor().newInstance();
        }
        return bean;
    }

    private void scanComponents(Path componentScanPath){
        if (componentScanPath.toFile().listFiles() == null) {
            throw new ComponentScanException(String.format("Path %s is not a directory.", componentScanPath));
        } else {
            readClasses(componentScanPath.toFile());
        }
    }

    private void readClasses(File file){
        File[] files = file.listFiles();

        if (files != null){
            for (File f : files){
                if (f.isDirectory()){
                    readClasses(f);
                } else {
                    String name = f.getPath();
                    if (!name.endsWith(".java")){
                        return;
                    } else {
                        int beginInd = name.indexOf(basePackageName);
                        int endInd = name.indexOf(".java");
                        String className = String.join(".", name.substring(beginInd, endInd).replaceAll("\\\\", "."));
                        try {
                            classes.add(Class.forName(className));
                        } catch (ClassNotFoundException e) {
                            throw new ComponentScanException(String.format("Error creating instance of class %s.", className));
                        }
                    }
                }
            }
        }
    }

    @Override
    protected Object getPrototypeBean(BeanMetadata metadata){
        try {
            return createBeanWithDependencies(metadata);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ignored) {

        }

        return null;
    }

    @Override
    public Object getBean(String beanName) {
        Object bean = MainBeanContainer.getBean(beanName);
        if (bean == null){
            throw new BeanNotFoundException(String.format("Bean with name \"%s\" not found", beanName));
        } else if (!(bean instanceof BeanMetadata)) {
            return bean;
        } else{
            if (((BeanMetadata) bean).getContextType().equals(ContextType.CONFIGURATION_DRIVEN)){
                return configurationClassDrivenContext.getBean(beanName);
            }

            return getPrototypeBean((BeanMetadata)bean);
        }
    }

    @Override
    public <T> T getBean(Class<T> clazz) {
        Object bean = MainBeanContainer.getBean(clazz);
        if (bean == null){
            throw new BeanNotFoundException(String.format("Bean with class \"%s\" not found", clazz));
        } else if (!(bean instanceof BeanMetadata)) {
            return (T)bean;
        } else{
            if (((BeanMetadata) bean).getContextType().equals(ContextType.CONFIGURATION_DRIVEN)){
                return configurationClassDrivenContext.getBean(clazz);
            } else{
                return (T)getPrototypeBean((BeanMetadata)bean);
            }
        }
    }
}
