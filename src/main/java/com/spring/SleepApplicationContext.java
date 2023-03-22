package com.spring;






import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class SleepApplicationContext {

    private  ConcurrentHashMap<String,Object> singletonObjects = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();

    private Class configClass;

    public SleepApplicationContext (Class configClass){
        this.configClass = configClass;
        scan(configClass);
        for (Map.Entry<String,BeanDefinition>  entry: beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();
            if (beanDefinition.getScope().equals("singleton")){
                Object bean = createBean(beanDefinition,beanName);
                singletonObjects.put(beanName,bean);
            }
        }

    }

    private void scan(Class configClass) {
        //解析配置类
        //解析这个注解-->扫描路径-->然后去扫描@ComponetScan("com.sleep.service")
        ComponetScan componetScanAnnotation = (ComponetScan) configClass.getDeclaredAnnotation(ComponetScan.class);
        String path = componetScanAnnotation.value();
        System.out.println("path:"+path);
        // 扫描
        // 1.根据扫描路径包名得到所有的类
        // 2.Bootstrap-->jre/lib
        // 3.Ext-->jre/extt/lib
        // 4.App-->classpath
        ClassLoader classLoader = SleepApplicationContext.class.getClassLoader();
        URL resource = classLoader.getResource(path);
        File file = new File(resource.getFile());
        if (file.isDirectory()){
            File[] files = file.listFiles();
            for (File f:files){
                String absolutePath = f.getAbsolutePath();
                if (absolutePath.endsWith(".class")){
                String className = absolutePath.substring(absolutePath.indexOf("com"), absolutePath.indexOf(".class"));
                String replace = className.replace("\\", ".");
                    System.out.println("absolutePath:"+absolutePath+"\nreplace:"+replace);
                    try {
                        Class<?> clazz = classLoader.loadClass(replace);
                        if (clazz.isAnnotationPresent(Component.class)) {
                            //生成bean对象
                            //解析类-->BeanDefinition
                            //判断当前bean是单例bean还是原型bean(prototype)
                            //BeanDefinition
                            // 判断类是不是实现接口
                            if (BeanPostProcessor.class.isAssignableFrom(clazz)){
                                BeanPostProcessor o = (BeanPostProcessor)clazz.getDeclaredConstructor().newInstance();
                                beanPostProcessorList.add(o);
                            }
                                Component componentAnnotation = clazz.getDeclaredAnnotation(Component.class);
                                String baenName = componentAnnotation.value();
                                BeanDefinition beanDefinition = new BeanDefinition();
                                beanDefinition.setClazz(clazz);
                                if (clazz.isAnnotationPresent(Scope.class)) {
                                    Scope declaredAnnotation = clazz.getDeclaredAnnotation(Scope.class);
                                    beanDefinition.setScope(declaredAnnotation.value());
                                } else {
                                    beanDefinition.setScope("singleton");
                                }
                                beanDefinitionMap.put(baenName, beanDefinition);
                        }
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException(e);
                    } catch (InstantiationException e) {
                        throw new RuntimeException(e);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                }

            }
        }
    }

    public Object getBean(String beanName){
        if (beanDefinitionMap.containsKey(beanName)){
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if (beanDefinition.getScope().equals("singleton")){
                Object o = singletonObjects.get(beanName);
                if (Objects.isNull(o)){
                    return createBean(beanDefinition,beanName);
                }
                return o;
            }else {
                //创建bean对象
               return createBean(beanDefinition,beanName);

            }

        }else {
            //不存在对应的bean
            throw new NullPointerException();
        }
    }
    public Object createBean(BeanDefinition beanDefinition,String beanName){
        Class clazz = beanDefinition.getClazz();
        try {
            Object instance = clazz.getDeclaredConstructor().newInstance();
            //依赖注入
            for (Field declaredField : clazz.getDeclaredFields()) {
                if (declaredField.isAnnotationPresent(Autowired.class)){
                    Object bean = getBean(declaredField.getName());
                    declaredField.setAccessible(true);
                    declaredField.set(instance, bean);
                }
            }
            //P7.Aware回调
            if (instance instanceof BeanNameAware){
                ((BeanNameAware)instance).setBeanName(beanName);
            }
            // 初始化后
            //可以增加数字排序
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
            }
            // 初始化
            if (instance instanceof InitializingBean){
                ((InitializingBean)instance).afterPropertiesSet();
            }
            //初始化前
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessAfterInitialization(instance,beanName);
            }
            return instance;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

