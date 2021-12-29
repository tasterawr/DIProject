package org.loktevik.di.context;

import org.loktevik.di.annotations.*;
import org.loktevik.di.exceptions.BeanNotFoundException;
import org.loktevik.di.exceptions.ComponentScanException;

import java.io.File;
import java.lang.reflect.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class AnnotationDrivenContext implements DependencyInjectionContext {
    private final List<Class<?>> classes;
    private final String basePackageName;

    {
        classes = new ArrayList<>();
        basePackageName = this.getClass().getName().split("\\.")[0];
    }

    public AnnotationDrivenContext(Path componentScanPath){
        File root = componentScanPath.toFile();

        if (root.listFiles() != null){
            readClasses(root);
        } else {
            throw new ComponentScanException(String.format("Path %s is not a directory.", componentScanPath));
        }

        List<Class<?>> componentClasses = classes.stream()
                .filter(clazz -> clazz.isAnnotationPresent(Component.class))
                .collect(Collectors.toList());

        for (Class<?> cl : componentClasses){
            BeanMetadata beanMetadata = new BeanMetadata();
            if (!cl.getAnnotation(Component.class).value().equals("")){
                beanMetadata.setName(cl.getAnnotation(Component.class).value());
            } else{
                String firstLetter = cl.getSimpleName().split("")[0].toLowerCase(Locale.ROOT);
                beanMetadata.setName(firstLetter + cl.getSimpleName().substring(1));
            }
            beanMetadata.setClazz(cl);
            if (cl.isAnnotationPresent(Prototype.class)){
                beanMetadata.setScope("prototype");
            } else {
                beanMetadata.setScope("singleton");
            }
            beanMetadata.setContextType(ContextType.ANNOTATION_DRIVEN);

            List<DependencyMetadata> dependencies = new ArrayList<>();
            for (Field field : cl.getDeclaredFields()) {
                if (field.isAnnotationPresent(AutoInject.class)){
                    DependencyMetadata dm = new DependencyMetadata();
                    dm.setName(field.getDeclaredAnnotation(AutoInject.class).value());
                    dm.setClazz(field.getType());
                    dm.setInjectType(InjectType.FIELD);
                    dependencies.add(dm);
                }
            }

            for (Constructor<?> cons : cl.getDeclaredConstructors()){
                if (cons.isAnnotationPresent(AutoInject.class)){
                    for (Parameter param : cons.getParameters()){
                        DependencyMetadata dm = new DependencyMetadata();
                        if (param.isAnnotationPresent(ByName.class)){
                            dm.setName(param.getDeclaredAnnotation(ByName.class).value());
                        }
                        dm.setClazz(param.getType());
                        dm.setInjectType(InjectType.CONSTRUCTOR);
                        dependencies.add(dm);
                    }
                }
            }

            for (Method m : cl.getDeclaredMethods()){
                if (m.isAnnotationPresent(AutoInject.class) && m.getParameters().length == 1 && m.getName().indexOf("set") == 0){
                    DependencyMetadata dm = new DependencyMetadata();
                    dm.setName(m.getDeclaredAnnotation(AutoInject.class).value());
                    dm.setClazz(m.getParameterTypes()[0]);
                    dm.setInjectType(InjectType.SETTER);
                    dependencies.add(dm);
                }
            }

            beanMetadata.setAnnotationDependencies(dependencies);
            MainBeanContainer.addBeanMetadata(beanMetadata);
        }

        List<BeanMetadata> metadataList = MainBeanContainer.getMetadataList().stream()
                .filter(beanMetadata -> beanMetadata.getContextType().equals(ContextType.ANNOTATION_DRIVEN))
                .collect(Collectors.toList());

        for (BeanMetadata beanMetadata : metadataList){
            createBeanFromMetadata(beanMetadata);
        }
    }

    private void createBeanFromMetadata(BeanMetadata beanMetadata) {
        try {
            if (beanMetadata.getScope().equals("singleton")){
                if (beanMetadata.getAnnotationDependencies().size() == 0){
                    Object bean = beanMetadata.getClazz().getDeclaredConstructor().newInstance();
                    MainBeanContainer.addSingletonBean(beanMetadata.getName(), bean);
                } else {
                    List<Object> fieldDependencyBeans = new ArrayList<>();
                    List<Object> consDependencyBeans = new ArrayList<>();
                    List<Object> setterDependencyBeans = new ArrayList<>();
                    for (DependencyMetadata dm : beanMetadata.getAnnotationDependencies()){
                        Object dependencyBean = MainBeanContainer.getBean(dm.getClazz());
                        if (dependencyBean == null){
                            BeanMetadata dependencyMetadata = MainBeanContainer.getMetadataList().stream().filter(metadata -> metadata.getClazz().equals(dm.getClazz())).findFirst().orElse(null);
                            if (dependencyMetadata != null) {
                                createBeanFromMetadata(dependencyMetadata);
                                dependencyBean = MainBeanContainer.getBean(dm.getClazz());
                            }
                        }

                        if (dm.getInjectType().equals(InjectType.FIELD)){
                            fieldDependencyBeans.add(dependencyBean);
                        } else if (dm.getInjectType().equals(InjectType.CONSTRUCTOR)){
                            consDependencyBeans.add(dependencyBean);
                        } else {
                            setterDependencyBeans.add(dependencyBean);
                        }
                    }

                    Object bean = null;
                    if (consDependencyBeans.size() != 0){
                        List<? extends Class<?>> constructorParamTypes = beanMetadata.getAnnotationDependencies().stream()
                                .filter(dm -> dm.getInjectType().equals(InjectType.CONSTRUCTOR))
                                .map(DependencyMetadata::getClazz)
                                .collect(Collectors.toList());

                        Constructor<?> declaredConstructor = beanMetadata.getClazz().getDeclaredConstructor(constructorParamTypes.toArray(new Class[0]));
                        bean = declaredConstructor.newInstance(consDependencyBeans.toArray());
                    } else {
                        bean = beanMetadata.getClazz().getDeclaredConstructor().newInstance();
                    }

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

                    MainBeanContainer.addSingletonBean(beanMetadata.getName(), bean);
                }
            } else if (beanMetadata.getScope().equals("prototype")){
                MainBeanContainer.addPrototypeBean(beanMetadata.getName(), beanMetadata);
            }
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException | InstantiationException ignored) {

        }
    }

    public void readClasses(File file){
        File[] files = file.listFiles();

        if (files != null){
            for (File f : files){
                if (f.isDirectory()){
                    readClasses(f);
                } else {
                    String name = f.getPath();
                    if (!name.endsWith(".java")){
                        continue;
                    } else {
                        int beginInd = name.indexOf(basePackageName);
                        int endInd = name.indexOf(".java");
                        String className = String.join(".", name.substring(beginInd, endInd).replaceAll("\\\\", "."));
                        try {
                            classes.add(Class.forName(className));
                        } catch (ClassNotFoundException e) {
                            throw new ComponentScanException(String.format("Error creating class %s.", className));
                        }
                    }
                }
            }
        }
    }

    @Override
    public Object getBean(String beanName) {
        Object bean = MainBeanContainer.getBean(beanName);
        if (bean == null){
            throw new BeanNotFoundException(String.format("Bean with name \"%s\" not found", beanName));
        } else {
            return bean;
        }
    }

    @Override
    public Object getBean(Class<?> clazz) {
        Object bean = MainBeanContainer.getBean(clazz);
        if (bean == null){
            throw new BeanNotFoundException(String.format("Bean with class \"%s\" not found", clazz));
        } else {
            return bean;
        }
    }
}
