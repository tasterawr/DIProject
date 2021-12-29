package org.loktevik.di;

import org.loktevik.di.annotations.Bean;
import org.loktevik.di.annotations.Configuration;
import org.loktevik.di.annotations.Ordered;
import org.loktevik.di.annotations.Prototype;

@Configuration
@Ordered
public class AppConfig {

    @Bean
    @Prototype
    public TestClassB testClassB(TestClassA testClassA){
        return new TestClassB(testClassA);
    }

    @Bean(className = "testClassA")
    @Prototype
    public TestClassA testClassA(){
        return new TestClassA();
    }

//    @Bean
//    public TestClassC testClassC(TestClassA testClassA, TestClassB testClassB){
//        return new TestClassC(testClassA, testClassB);
//    }

}
