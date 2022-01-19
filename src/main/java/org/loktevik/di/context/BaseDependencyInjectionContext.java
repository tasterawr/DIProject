package org.loktevik.di.context;

public abstract class BaseDependencyInjectionContext implements DependencyInjectionContext {

    public final DependencyInjectionContext run(){
        for (BeanMetadata metadata : MainBeanContainer.getMetadataList()){
            if (!metadata.isBeanCreated())
                createBean(metadata);
        }

        return this;
    }

    protected final void createBean(BeanMetadata beanMetadata){
        beanMetadata.getContext().createBeanFromMetadata(beanMetadata);
        beanMetadata.setBeanCreated(true);
    }

    protected abstract void createBeanFromMetadata(BeanMetadata beanMetadata);

    protected abstract Object getPrototypeBean(BeanMetadata metadata);

    protected final Object getDependencyBean(DependencyMetadata dm){
        Object dependencyBean;
        if (!"".equals(dm.getName()) && dm.getName() != null){
            dependencyBean = MainBeanContainer.getBean(dm.getName());
        } else {
            dependencyBean = MainBeanContainer.getBean(dm.getClazz());
        }
        if (dependencyBean == null){
            BeanMetadata dependencyBeanMetadata = MainBeanContainer.getBeanMetadata(dm);

            if (dependencyBeanMetadata != null) {
                createBean(dependencyBeanMetadata);
                dependencyBean = MainBeanContainer.getBean(dm.getClazz());
            }
        } else if (dependencyBean instanceof BeanMetadata){
            dependencyBean = getPrototypeBean((BeanMetadata) dependencyBean);
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
