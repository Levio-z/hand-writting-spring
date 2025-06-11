package com.sleep.service;

import com.spring.*;

@Component("userService")
public class UserService implements BeanNameAware, InitializingBean,UserInterface {
    @Autowired
    private OrderService orderService;
    private String beanName;

    @Override
    public void setBeanName(String beanName) {
        this.beanName =beanName;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("Initialization");
    }

    @Override
    public void test(){
        System.out.println(orderService);
    }

}
