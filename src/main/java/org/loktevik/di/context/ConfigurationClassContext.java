package org.loktevik.di.context;

import org.loktevik.di.annotations.Bean;
import org.loktevik.di.annotations.Prototype;
import org.loktevik.di.exceptions.BeanNotFoundException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigurationClassContext implements DependencyInjectionContext{

    public ConfigurationClassContext(Class<?>... configurationClasses) {
        for (Class<?> config : configurationClasses){

            //создаем объект класса конфигурации
            Object configInstance = null;
            try {
                configInstance = config.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {

            }

            //берем все методы, помеченные аннотацией @Bean
            List<Method> methods = Arrays.stream(config.getDeclaredMethods())
                    .filter(m -> m.isAnnotationPresent(Bean.class))
                    .sorted(Comparator.comparingInt(Method::getParameterCount))
                    .collect(Collectors.toList());

            //для каждого метода
            for (Method m : methods){

                //если у @Bean указано classname, устанавливаем, иначе ставим название метода
                BeanMetadata beanMetadata = new BeanMetadata();
                if (!m.getDeclaredAnnotation(Bean.class).className().equals("")){
                    beanMetadata.setName(m.getDeclaredAnnotation(Bean.class).className());
                } else {
                    beanMetadata.setName(m.getName());
                }

                //устанавливаем параметры BeanMetadata
                beanMetadata.setName(m.getName());
                beanMetadata.setClazz(m.getReturnType());
                beanMetadata.setMethod(m);
                beanMetadata.setInvokeOn(configInstance);
                beanMetadata.setContextType(ContextType.CONFIGURATION_DRIVEN);

                if (m.isAnnotationPresent(Prototype.class)){
                    beanMetadata.setScope("prototype");
                } else {
                    beanMetadata.setScope("singleton");
                }

                for (Parameter p : m.getParameters()){
                    beanMetadata.getDependencies().add(p.getType());
                }

                //сохраняем созданный BeanMetadata объект
                MainBeanContainer.addBeanMetadata(beanMetadata);
            }
        }

        List<BeanMetadata> metadataList = MainBeanContainer.getMetadataList().stream()
                .filter(beanMetadata -> beanMetadata.getContextType().equals(ContextType.CONFIGURATION_DRIVEN))
                .collect(Collectors.toList());

        for (BeanMetadata beanMetadata : metadataList){
            createBeanFromMetadata(beanMetadata);
        }
    }

    private void createBeanFromMetadata(BeanMetadata beanMetadata){
        try {
            if (beanMetadata.getScope().equals("singleton")){
                if (beanMetadata.getDependencies().size() == 0){
                    Object bean = beanMetadata.getMethod().invoke(beanMetadata.getInvokeOn());
                    MainBeanContainer.addSingletonBean(beanMetadata.getName(), bean);
                } else {
                    List<Object> dependencyBeans = new ArrayList<>();
                    for (Class<?> dependency : beanMetadata.getDependencies()){
                        Object dependencyBean = MainBeanContainer.getBean(dependency);
                        if (dependencyBean == null){
                            BeanMetadata dependencyMetadata = MainBeanContainer.getMetadataList().stream().filter(metadata -> metadata.getClazz().equals(dependency)).findFirst().orElse(null);
                            if (dependencyMetadata != null) {
                                createBeanFromMetadata(dependencyMetadata);
                                dependencyBean = MainBeanContainer.getBean(dependency);
                            }
                        }
                        dependencyBeans.add(dependencyBean);
                    }

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
