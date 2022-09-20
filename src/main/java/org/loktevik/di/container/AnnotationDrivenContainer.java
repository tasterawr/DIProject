package org.loktevik.di.container;

import org.loktevik.di.annotations.*;
import org.loktevik.di.container.handlers.BeanMetadataDefaultHandlerClassesContainer;
import org.loktevik.di.container.handlers.BeanMetadataHandler;
import org.loktevik.di.exceptions.*;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AnnotationDrivenContainer extends BaseDependencyInjectionContainer implements DependencyInjectionContainer {
    private final List<Class<?>> classes;
    private final String basePackageName;
    private final List<BeanMetadataHandler> handlers;
    private ConfigurationClassDrivenContainer configurationClassDrivenContainer = null;

    {
        classes = new ArrayList<>();
        basePackageName = this.getClass().getName().split("\\.")[0];
        handlers = new ArrayList<>();
    }

    public AnnotationDrivenContainer(Path componentScanPath){
        scanComponents(componentScanPath);
        initDefaultMetadataHandlers();
        initCustomMetadataHandlers();
        List<Class<?>> componentClasses = getComponentClasses();

        for (Class<?> cl : componentClasses){
            BeanMetadata beanMetadata = new BeanMetadata();
            beanMetadata.setClazz(cl);
            beanMetadata.setContainerType(ContainerType.ANNOTATION_DRIVEN);
            beanMetadata.setContainer(this);
            beanMetadata.setAnnotationDependencies(new ArrayList<>());

            for (BeanMetadataHandler handler : handlers){
                handler.handle(beanMetadata);
            }
            MainBeanRepository.addBeanMetadata(beanMetadata);
        }

        List<Class<?>> includedConfigurations = getIncludedConfigurations();
        if (includedConfigurations.size() != 0){
            configurationClassDrivenContainer = new ConfigurationClassDrivenContainer(includedConfigurations.toArray(new Class<?>[0]));
        }
    }

    private void initDefaultMetadataHandlers() {
        Class<?>[] defaultHandlerClasses = BeanMetadataDefaultHandlerClassesContainer.class.getDeclaredClasses();
        Object containerClassInstance;
        try {
            containerClassInstance = BeanMetadataDefaultHandlerClassesContainer.class.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new AnnotationDrivenContainerException(String.format("Could not create instance of default bean metadata classes container %s.", BeanMetadataDefaultHandlerClassesContainer.class));
        }

        for (Class<?> cl : defaultHandlerClasses){
            try {
                Constructor<?> constructor = cl.getDeclaredConstructor(BeanMetadataDefaultHandlerClassesContainer.class);
                constructor.setAccessible(true);
                handlers.add((BeanMetadataHandler) constructor.newInstance(containerClassInstance));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new BeanMetadataHandlerException(String.format("Could not create instance of default bean metadata handler from class %s.", cl), e);
            }
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

    private void initCustomMetadataHandlers() {
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
                MainBeanRepository.addSingletonBean(beanMetadata.getName(), bean);
            } else if (beanMetadata.getScope().equals("prototype")){
                MainBeanRepository.addPrototypeBean(beanMetadata.getName(), beanMetadata);
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
        List<Field> requiredFields = Arrays.stream(beanMetadata.getClazz().getDeclaredFields()).filter(field -> field.isAnnotationPresent(AutoInject.class)).collect(Collectors.toList());
        if (fieldDependencyBeans.size() != 0){
            for (int i = 0; i < fieldDependencyBeans.size(); i++){
                Field field = requiredFields.get(i);
                field.setAccessible(true);
                Object dependency = fieldDependencyBeans.get(i);
                field.set(bean, dependency);
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
        Object bean = MainBeanRepository.getBean(beanName);
        if (bean == null){
            throw new BeanNotFoundException(String.format("Bean with name \"%s\" not found", beanName));
        } else if (!(bean instanceof BeanMetadata)) {
            return bean;
        } else{
            if (((BeanMetadata) bean).getContainerType().equals(ContainerType.CONFIGURATION_DRIVEN)){
                return configurationClassDrivenContainer.getBean(beanName);
            }

            return getPrototypeBean((BeanMetadata)bean);
        }
    }

    @Override
    public <T> T getBean(Class<T> clazz) {
        Object bean = MainBeanRepository.getBean(clazz);
        if (bean == null){
            throw new BeanNotFoundException(String.format("Bean with class \"%s\" not found", clazz));
        } else if (!(bean instanceof BeanMetadata)) {
            return (T)bean;
        } else{
            if (((BeanMetadata) bean).getContainerType().equals(ContainerType.CONFIGURATION_DRIVEN)){
                return configurationClassDrivenContainer.getBean(clazz);
            } else{
                return (T)getPrototypeBean((BeanMetadata)bean);
            }
        }
    }
}
