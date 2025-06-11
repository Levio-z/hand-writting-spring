package com.sleep;

import com.sleep.service.UserInterface;
import com.sleep.service.UserService;
import com.spring.SleepApplicationContext;

public class Test {
    public static void main(String[] args) {
        SleepApplicationContext applicationContext = new SleepApplicationContext(AppConfig.class);
        UserInterface userInterface = (UserInterface)applicationContext.getBean("userService");
        System.out.println("=========== Test Execution userInterface.toString() ============");
        System.out.println("UserInterface instance: " + userInterface);
        System.out.println("=========== Test Execution Completed ============\r\n");

        System.out.println("=========== Test Execution userInterface.test() ============");
        userInterface.test();
        System.out.println("=========== Test Execution Completed ============");    }
}
