package org.loktevik.di;

import org.loktevik.di.context.AnnotationDrivenContext;
import org.loktevik.di.context.ConfigurationClassContext;
import org.loktevik.di.context.DependencyInjectionContext;

import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        DependencyInjectionContext context = new AnnotationDrivenContext(Paths.get(".","src", "main", "java", "org", "loktevik", "di"));
        Object testClassC = context.getBean("testClassC");
        Object testClassB = context.getBean("testClassB");
        Object classA = context.getBean("testClassA");
        return;
    }
}
