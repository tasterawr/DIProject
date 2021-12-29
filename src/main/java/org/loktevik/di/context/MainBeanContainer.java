package org.loktevik.di.context;

import org.loktevik.di.exceptions.BeanClarificationException;
import org.loktevik.di.exceptions.BeanNotFoundException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

class MainBeanContainer {
    private static final List<BeanMetadata> metadataList;
    private static final Map<String, Object> singletonBeanMap;
    private static final Map<String, BeanMetadata> prototypeBeanMap;

    static {
        metadataList = new ArrayList<>();
        singletonBeanMap = new HashMap<>();
        prototypeBeanMap = new HashMap<>();
    }

    public static Object getBean(String beanName){
        Object bean = singletonBeanMap.get(beanName);
        BeanMetadata beanMetadata = prototypeBeanMap.get(beanName);
        if (bean != null && beanMetadata != null){
            throw new BeanClarificationException(String.format("More than one bean for name \"%s\" found", beanName));
        } else if (bean != null){
            return bean;
        } else if (beanMetadata != null) {
            return getBeanFromMetadata(beanMetadata);
        }

        return null;
    }

    public static Object getBean(Class<?> clazz){
        List<Object> singletonBeans = singletonBeanMap.values().stream()
                .filter(bean -> bean.getClass().equals(clazz))
                .collect(Collectors.toList());

        List<BeanMetadata> prototypeBeans = prototypeBeanMap.values().stream()
                .filter(metadata -> metadata.getClazz().equals(clazz))
                .collect(Collectors.toList());

        if ((singletonBeans.size() != 0 && prototypeBeans.size() != 0)
                || singletonBeans.size() > 1
                || prototypeBeans.size() > 1){
            throw new BeanClarificationException(String.format("More than one bean for class \"%s\" found", clazz.getName()));
        } else if (singletonBeans.size() == 1){
            return singletonBeans.get(0);
        } else if (prototypeBeans.size() == 1){
            BeanMetadata metadata = prototypeBeans.get(0);
            return getBeanFromMetadata(metadata);
        }

        return null;
    }

    private static Object getBeanFromMetadata(BeanMetadata beanMetadata){
        List<Object> dependencyBeans = new ArrayList<>();
        for (Class<?> cl : beanMetadata.getDependencies()){
            dependencyBeans.add(getBean(cl));
        }

        try {
            return beanMetadata.getMethod().invoke(beanMetadata.getInvokeOn(), dependencyBeans.toArray());
        } catch (InvocationTargetException | IllegalAccessException e){
            System.out.println(e.getMessage());
        }

        return null;
    }

    public static void addSingletonBean(String beanName, Object bean){
        singletonBeanMap.put(beanName, bean);
    }

    public static void addPrototypeBean(String beanName, BeanMetadata beanMetadata){
        prototypeBeanMap.put(beanName, beanMetadata);
    }

    public static void addBeanMetadata(BeanMetadata beanMetadata){
        metadataList.add(beanMetadata);
    }

    public static List<BeanMetadata> getMetadataList(){
        return metadataList;
    }
}
