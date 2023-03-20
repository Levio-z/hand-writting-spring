package com.spring;

public class SleepApplicationContext {
    private Class configClass;

    public SleepApplicationContext (Class configClass){
        this.configClass = configClass;
        //解析配置类
        //解析这个注解-->扫描路径-->然后去扫描@ComponetScan("com.sleep.service")

    }

    public Object getBean(String beanName){
        return null;
    }
}
