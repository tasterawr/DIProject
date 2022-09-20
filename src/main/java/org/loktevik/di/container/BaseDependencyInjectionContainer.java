package org.loktevik.di.container;

import org.loktevik.di.exceptions.BeanClarificationException;
import org.loktevik.di.exceptions.BeanCreationException;

public abstract class BaseDependencyInjectionContainer implements DependencyInjectionContainer {

    @Override
    public final DependencyInjectionContainer run(){
        for (BeanMetadata metadata : MainBeanRepository.getMetadataList()){
            if (!metadata.isBeanCreated())
                createBean(metadata);
        }

        return this;
    }

    protected final void createBean(BeanMetadata beanMetadata){
        beanMetadata.getContainer().createBeanFromMetadata(beanMetadata);
        beanMetadata.setBeanCreated(true);
    }

    protected abstract void createBeanFromMetadata(BeanMetadata beanMetadata);

    protected abstract Object getPrototypeBean(BeanMetadata metadata);

    protected final Object getDependencyBean(DependencyMetadata dm){
        boolean hasName = !"".equals(dm.getName()) && dm.getName() != null;
        Object dependencyBean = hasName ? MainBeanRepository.getBean(dm.getName()) : MainBeanRepository.getBean(dm.getClazz());

        if (dependencyBean == null){
            BeanMetadata dependencyBeanMetadata = MainBeanRepository.getBeanMetadata(dm);

            if (dependencyBeanMetadata != null) {
                createBean(dependencyBeanMetadata);
                dependencyBean = hasName ? MainBeanRepository.getBean(dm.getName()) : MainBeanRepository.getBean(dm.getClazz());
            }
        } else if (dependencyBean instanceof BeanMetadata){
            dependencyBean = getPrototypeBean((BeanMetadata) dependencyBean);
        } else {
            if (!dm.getClazz().isAssignableFrom(dependencyBean.getClass())){
                throw new BeanCreationException(String.format("Cannot inject bean with name \"%s\". Expected bean of type %s, but got %s", dm.getName(), dm.getClazz(), dependencyBean.getClass()));
            }
        }

        return dependencyBean;
    }

    @Override
    public Object getBean(String beanName) {
        return null;
    }

    @Override
    public <T> T getBean(Class<T> clazz) {
        return null;
    }

    @Override
    public <T> T getBean(String beanName, Class<T> clazz) {
        return null;
    }
}
