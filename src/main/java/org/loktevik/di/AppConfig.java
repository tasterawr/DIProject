package org.loktevik.di;

import org.loktevik.di.annotations.*;

@Configuration
@Included
public class AppConfig {

//    @Bean
//    @Prototype
//    public TestClassB testClassB(TestClassA testClassA){
//        return new TestClassB(testClassA);
//    }
//
//    @Bean(className = "testClassA")
//    @Prototype
//    public TestClassA testClassA(){
//        return new TestClassA();
//    }
//
    @Bean
    @Prototype
    public TestClassB testClassB(TestClassA testClassA){
        TestClassB testClassB = new TestClassB(testClassA);
        return testClassB;
    }


    @Bean(beanName = "helloWorld")
    public String value(){
        return "Hello World";
    }

    @Bean(beanName = "partyNow")
    public String party(){
        return "PARTY NOW";
    }

}
