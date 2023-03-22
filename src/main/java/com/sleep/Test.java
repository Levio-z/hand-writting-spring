package com.sleep;

import com.sleep.service.UserInterface;
import com.sleep.service.UserService;
import com.spring.SleepApplicationContext;

public class Test {
    public static void main(String[] args) {
        SleepApplicationContext applicationContext = new SleepApplicationContext(AppConfig.class);
        UserInterface userInterface = (UserInterface)applicationContext.getBean("userService");
        System.out.println(userInterface);
        userInterface.test();
    }
}
