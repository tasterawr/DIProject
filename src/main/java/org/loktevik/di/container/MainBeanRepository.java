package org.loktevik.di.container;

import org.loktevik.di.exceptions.BeanClarificationException;
import org.loktevik.di.exceptions.BeanNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class MainBeanRepository {
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
        } else {
            return beanMetadata;
        }
    }

    public static Object getBean(Class<?> clazz){
        List<Object> singletonBeans = singletonBeanMap.values().stream()
                .filter(bean -> clazz.isAssignableFrom(bean.getClass()))
                .collect(Collectors.toList());

        List<BeanMetadata> prototypeBeans = prototypeBeanMap.values().stream()
                .filter(metadata -> clazz.isAssignableFrom(metadata.getClazz()))
                .collect(Collectors.toList());

        if ((singletonBeans.size() != 0 && prototypeBeans.size() != 0)
                || singletonBeans.size() > 1
                || prototypeBeans.size() > 1){
            throw new BeanClarificationException(String.format("More than one bean for class \"%s\" found", clazz.getName()));
        } else if (singletonBeans.size() == 1){
            return singletonBeans.get(0);
        } else if (prototypeBeans.size() == 1){
            return prototypeBeans.get(0);
        }

        return null;
    }

    protected static BeanMetadata getBeanMetadata(DependencyMetadata dm){
        List<BeanMetadata> result;
        if (!"".equals(dm.getName()) && dm.getName() != null){
            result = getMetadataList().stream().filter(metadata -> metadata.getName().equals(dm.getName())).collect(Collectors.toList());
        } else{
            result = getMetadataList().stream().filter(metadata -> dm.getClazz().isAssignableFrom(metadata.getClazz())).collect(Collectors.toList());
        }

        if (result.size() > 1){
            throw new BeanClarificationException(String.format("More than one bean for class \"%s\" found", dm.getClazz()));
        } else if (result.size() == 0 && !"".equals(dm.getName())){
            throw new BeanNotFoundException(String.format("Bean with name \"%s\" not found", dm.getName()));
        } else if (result.size() == 0){
            throw new BeanNotFoundException(String.format("Bean with class \"%s\" not found", dm.getClazz()));
        }

        return result.get(0);
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
