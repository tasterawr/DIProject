package org.loktevik.di;

import org.loktevik.di.annotations.Component;

@Component("classA")
public class TestClassA implements ITestClass{
    public void action(){
        System.out.println("In class A");
    }
}
