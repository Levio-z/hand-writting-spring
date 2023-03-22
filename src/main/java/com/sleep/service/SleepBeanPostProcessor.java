package com.sleep.service;

import com.spring.BeanPostProcessor;
import com.spring.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Component("sleepBeanPostProcessor")
public class SleepBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        System.out.println("初始化前");
        if (beanName.equals("userService")){
            ((UserService)bean).setBeanName("睡觉好棒");
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("初始化后");
        if (beanName.equals("userService")){
            Object proxyInstance = Proxy.newProxyInstance(SleepBeanPostProcessor.class.getClassLoader(), bean.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    System.out.println("代理逻辑");
                    //找切点
                    return method.invoke(bean,args);
                }
            });
            return proxyInstance;
        }
        return bean;
    }
}
