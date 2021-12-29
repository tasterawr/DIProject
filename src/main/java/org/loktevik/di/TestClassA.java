package org.loktevik.di;

import org.loktevik.di.annotations.Component;

@Component
public class TestClassA {
    public void action(){
        System.out.println("In class A");
    }
}
