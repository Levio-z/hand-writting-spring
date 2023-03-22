package com.spring;

public class BeanDefinition {
    private Class clazz;
    private String scope;

    private String beanName;

    public BeanDefinition(Class clazz, String scope,String beanName) {
        this.clazz = clazz;
        this.scope = scope;
        this.beanName = beanName;
    }

    public BeanDefinition() {
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }
}
