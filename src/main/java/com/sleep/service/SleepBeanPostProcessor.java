package com.sleep.service;

import com.spring.BeanNameAware;
import com.spring.BeanPostProcessor;
import com.spring.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Component("sleepBeanPostProcessor")
public class SleepBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if (beanName.equals("userService")) {
            System.out.println("Before initialization");
            ((UserService)bean).setBeanName("Sleeping is great");
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (beanName.equals("userService")){
            System.out.println("After initialization");
            Object proxyInstance = Proxy.newProxyInstance(SleepBeanPostProcessor.class.getClassLoader(), bean.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    System.out.println(beanName+":Proxy logic");
                    // Find the cut point
                    return method.invoke(bean,args);
                }
            });
            return proxyInstance;
        }
        return bean;
    }
}
