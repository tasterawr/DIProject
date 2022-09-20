package org.loktevik.di;

import org.loktevik.di.annotations.AutoInject;
import org.loktevik.di.annotations.ByName;
import org.loktevik.di.annotations.Component;

@Component("classB")
public class TestClassB implements ITestClass{
    private ITestClass testClassA;

    @AutoInject
    public TestClassB(ITestClass a){
        testClassA = a;
    }

    public void action(){
        testClassA.action();
        System.out.println("In class B");
    }
}
