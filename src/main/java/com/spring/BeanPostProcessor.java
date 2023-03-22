//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.spring;



public interface BeanPostProcessor {
     Object postProcessBeforeInitialization(Object bean, String beanName);
     Object postProcessAfterInitialization(Object bean, String beanName);
}
