package org.loktevik.di;

import org.loktevik.di.context.AnnotationDrivenContext;
import org.loktevik.di.context.ConfigurationClassDrivenContext;
import org.loktevik.di.context.DependencyInjectionContext;

import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        DependencyInjectionContext context = new AnnotationDrivenContext(Paths.get(".","src", "main", "java", "org", "loktevik", "di")).run();
        TestClassC testClassC = (TestClassC)context.getBean("testClassC");
        Object secodTestClassC = context.getBean("testClassC");
        testClassC.action();
        Object testClassB = context.getBean("testClassB");
        Object classA = context.getBean("testClassA");


//        DependencyInjectionContext context = new ConfigurationClassDrivenContext(AppConfig.class).run();
//        TestClassC testClassC = context.getBean("testClassC", TestClassC.class);
//        Object secodTestClassC = context.getBean("testClassC");
//        testClassC.action();
//        Object testClassB = context.getBean("testClassB");
//        Object classA = context.getBean("testClassA");
//        System.out.println(context.getBean("partyNow"));
        return;
    }
}
