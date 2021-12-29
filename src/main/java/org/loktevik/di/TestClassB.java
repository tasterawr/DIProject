package org.loktevik.di;

import org.loktevik.di.annotations.AutoInject;
import org.loktevik.di.annotations.Component;

@Component
public class TestClassB {
    private TestClassA testClassA;

    @AutoInject
    public TestClassB(TestClassA a){
        testClassA = a;
    }

    public void action(){
        testClassA.action();
        System.out.println("In class B");
    }
}
