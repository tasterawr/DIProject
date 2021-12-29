package org.loktevik.di;

import org.loktevik.di.annotations.AutoInject;
import org.loktevik.di.annotations.Component;

@Component
public class TestClassC {
    private TestClassA testClassA;

    private TestClassB testClassB;

    @AutoInject
    public TestClassC(TestClassA testClassA){
        this.testClassA = testClassA;
    }

    public void action(){
        testClassA.action();
        testClassB.action();
        System.out.println("In class C");
    }

    @AutoInject
    public void setTestClassB(TestClassB testClassB) {
        this.testClassB = testClassB;
    }
}
