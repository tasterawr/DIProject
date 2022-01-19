package org.loktevik.di.context;

import org.loktevik.di.annotations.Bean;
import org.loktevik.di.annotations.ByName;
import org.loktevik.di.annotations.Prototype;
import org.loktevik.di.exceptions.BeanNotFoundException;
import org.loktevik.di.exceptions.ConfigurationClassDrivenContextException;
import org.loktevik.di.exceptions.ContextException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigurationClassDrivenContext extends BaseDependencyInjectionContext implements DependencyInjectionContext{

    public ConfigurationClassDrivenContext(Class<?>... configurationClasses) {
        for (Class<?> config : configurationClasses){

            Object configInstance = null;
            try {
                configInstance = config.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                throw new ConfigurationClassDrivenContextException(String.format("Couldn't create instance of configuration class %s.", config), e);
            }

            for (Method m : getConfigurationMethods(config)){
                BeanMetadata beanMetadata = new BeanMetadata();
                if (!m.getDeclaredAnnotation(Bean.class).className().equals("")){
                    beanMetadata.setName(m.getDeclaredAnnotation(Bean.class).className());
                } else {
                    beanMetadata.setName(m.getName());
                }
                beanMetadata.setClazz(m.getReturnType());
                beanMetadata.setMethod(m);
                beanMetadata.setInvokeOn(configInstance);
                beanMetadata.setContextType(ContextType.CONFIGURATION_DRIVEN);
                beanMetadata.setContext(this);

                if (m.isAnnotationPresent(Prototype.class)){
                    beanMetadata.setScope("prototype");
                } else {
                    beanMetadata.setScope("singleton");
                }

                for (Parameter p : m.getParameters()){
                    DependencyMetadata dm = new DependencyMetadata();
                    dm.setClazz(p.getType());
                    if (p.isAnnotationPresent(ByName.class)){
                        dm.setName(p.getDeclaredAnnotation(ByName.class).value());
                    }
                    beanMetadata.getAnnotationDependencies().add(dm);
                }

                MainBeanContainer.addBeanMetadata(beanMetadata);
            }
        }
    }

    private List<Method> getConfigurationMethods(Class<?> config) {
        return Arrays.stream(config.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(Bean.class))
                .sorted(Comparator.comparingInt(Method::getParameterCount))
                .collect(Collectors.toList());
    }

    @Override
    protected void createBeanFromMetadata(BeanMetadata beanMetadata){
        try {
            if (beanMetadata.getScope().equals("singleton")){
                if (beanMetadata.getAnnotationDependencies().size() == 0){
                    Object bean = beanMetadata.getMethod().invoke(beanMetadata.getInvokeOn());
                    MainBeanContainer.addSingletonBean(beanMetadata.getName(), bean);
                } else {
                    List<Object> dependencyBeans = new ArrayList<>();
                    addDependenciesToList(beanMetadata, dependencyBeans);

                    Object bean = beanMetadata.getMethod().invoke(beanMetadata.getInvokeOn(), dependencyBeans.toArray());
                    MainBeanContainer.addSingletonBean(beanMetadata.getName(), bean);
                }
            } else if (beanMetadata.getScope().equals("prototype")){
                MainBeanContainer.addPrototypeBean(beanMetadata.getName(), beanMetadata);
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void addDependenciesToList(BeanMetadata beanMetadata, List<Object> dependencyBeans) {
        for (DependencyMetadata dm : beanMetadata.getAnnotationDependencies()){
            Object dependencyBean = getDependencyBean(dm);

            dependencyBeans.add(dependencyBean);
        }
    }

    @Override
    protected Object getPrototypeBean(BeanMetadata beanMetadata){
        List<Object> dependencyBeans = new ArrayList<>();
        for (DependencyMetadata dm : beanMetadata.getAnnotationDependencies()){
            if (!"".equals(dm.getName()) && dm.getName() != null){
                dependencyBeans.add(getBean(dm.getName()));
            } else {
                dependencyBeans.add(getBean(dm.getClazz()));
            }
        }

        try {
            return beanMetadata.getMethod().invoke(beanMetadata.getInvokeOn(), dependencyBeans.toArray());
        } catch (InvocationTargetException | IllegalAccessException ignored){

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
            return getPrototypeBean((BeanMetadata) bean);
        }
    }

    public <T> T getBean(Class<T> clazz) {
        Object bean = MainBeanContainer.getBean(clazz);
        if (bean == null){
            throw new BeanNotFoundException(String.format("Bean with class \"%s\" not found", clazz));
        } else if (!(bean instanceof BeanMetadata)) {
            return (T)bean;
        } else{
            return (T)getPrototypeBean((BeanMetadata) bean);
        }
    }

    @Override
    public <T> T getBean(String beanName, Class<T> clazz) {
        return (T)getBean(beanName);
    }
}
