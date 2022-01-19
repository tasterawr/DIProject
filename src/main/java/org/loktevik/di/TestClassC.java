package org.loktevik.di;

import org.loktevik.di.annotations.*;

@Component
public class TestClassC {
    private TestClassA testClassA;

    private TestClassB testClassB;

    @AutoInject("helloWorld")
    private String value;

    @AutoInject
    public TestClassC(TestClassA testClassA){
        this.testClassA = testClassA;
    }

    public void action(){
        testClassA.action();
        testClassB.action();
        System.out.println("In class C. Value: " + value);
    }

    @AutoInject
    public void setTestClassB(TestClassB testClassB) {
        this.testClassB = testClassB;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
