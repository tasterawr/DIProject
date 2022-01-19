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
//    @Bean
//    @Prototype
//    public TestClassC testClassC(TestClassA testClassA, TestClassB testClassB, @ByName("partyNow") String s){
//        TestClassC testClassC = new TestClassC(testClassA);
//        testClassC.setTestClassB(testClassB);
//        testClassC.setValue(s);
//        return testClassC;
//    }

    @Bean(className = "helloWorld")
    public String value(){
        return "Hello World";
    }

    @Bean(className = "partyNow")
    public String party(){
        return "PARTY NOW";
    }

}
