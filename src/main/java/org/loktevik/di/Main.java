package org.loktevik.di;

import org.loktevik.di.container.AnnotationDrivenContainer;
import org.loktevik.di.container.DependencyInjectionContainer;

import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        DependencyInjectionContainer container = new AnnotationDrivenContainer(Paths.get(".","src", "main", "java", "org", "loktevik", "di")).run();
        TestClassC testClassC = (TestClassC)container.getBean("testClassC");
        Object secodTestClassC = container.getBean("testClassC");
        testClassC.action();
        Object testClassB = container.getBean("testClassB");


//        DependencyInjectionContainer container = new ConfigurationClassDrivenContainer(AppConfig.class).run();
//        TestClassC testClassC = container.getBean("testClassC", TestClassC.class);
//        Object secodTestClassC = container.getBean("testClassC");
//        testClassC.action();
//        Object testClassB = container.getBean("testClassB");
//        Object classA = container.getBean("testClassA");
//        System.out.println(container.getBean("partyNow"));
        return;
    }
}

public class Driver {
    private String name = "Ilya";
    private int age = 22;
    private RaceCar car = new RaceCar(200, "white");

    public Driver(ICar car) {

    }
}