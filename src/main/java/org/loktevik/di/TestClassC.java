package org.loktevik.di;

import org.loktevik.di.annotations.*;

@Component
public class TestClassC {
    private ITestClass testClassA;

    private ITestClass testClassB;

    @AutoInject("helloWorld")
    private String value;

    @AutoInject
    public TestClassC(@ByName("classA") ITestClass testClassA, ITestClass testClassB){
        this.testClassA = testClassA;
        this.testClassB = testClassB;
    }

    public void action(){
        testClassA.action();
        testClassB.action();
        System.out.println("In class C. Value: " + value);
    }

    public void setTestClassB(TestClassB testClassB) {
        this.testClassB = testClassB;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
