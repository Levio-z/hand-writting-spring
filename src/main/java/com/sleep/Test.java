package com.sleep;

import com.spring.SleepApplicationContext;

public class Test {
    public static void main(String[] args) {
        SleepApplicationContext applicationContext = new SleepApplicationContext(AppConfig.class);
        System.out.println(applicationContext.getBean("userService"));
        System.out.println(applicationContext.getBean("userService"));
        System.out.println(applicationContext.getBean("userService"));
    }
}
