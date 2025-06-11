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

    private ConcurrentHashMap<String,Object> singletonObjects = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();

    private Class configClass;

    public SleepApplicationContext (Class configClass){
        this.configClass = configClass;
        scan(configClass);
        for (Map.Entry<String,BeanDefinition> entry: beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();
            if (beanDefinition.getScope().equals("singleton")){
                Object bean = createBean(beanDefinition,beanName);
                singletonObjects.put(beanName,bean);
            }
        }

    }

    private void scan(Class configClass) {
        // Log the start of component scanning process
        System.out.println("========= Starting component scan ================");        // Parse configuration class
        // Parse this annotation --> scan path --> then scan @ComponentScan("com.sleep.service")
        ComponetScan componetScanAnnotation = (ComponetScan) configClass.getDeclaredAnnotation(ComponetScan.class);
        String path = componetScanAnnotation.value();
        System.out.println("path:"+path);
        // Scanning
        // 1. Get all classes based on the package name of the scan path
        // 2. Bootstrap --> jre/lib
        // 3. Ext --> jre/ext/lib
        // 4. App --> classpath
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
                            // Generate bean object
                            // Parse class --> BeanDefinition
                            // Determine if the current bean is a singleton or prototype
                            // BeanDefinition
                            // Determine if the class implements an interface
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
            System.out.println("========= Scan completed ================"+"\r\n");        }
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
                // Create bean object
               return createBean(beanDefinition,beanName);

            }

        }else {
            // No corresponding bean exists
            throw new NullPointerException();
        }
    }
    public Object createBean(BeanDefinition beanDefinition,String beanName){
        System.out.println("========= Starting bean creation ================" + beanName);        Class clazz = beanDefinition.getClazz();
        try {
            Object instance = clazz.getDeclaredConstructor().newInstance();
            // Dependency injection
            for (Field declaredField : clazz.getDeclaredFields()) {
                if (declaredField.isAnnotationPresent(Autowired.class)){
                    Object bean = getBean(declaredField.getName());
                    declaredField.setAccessible(true);
                    declaredField.set(instance, bean);
                }
            }
            // P7. Aware callback
            if (instance instanceof BeanNameAware){
                ((BeanNameAware)instance).setBeanName(beanName);
            }
            // After initialization
            // Can add numerical sorting
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
            }
            // Initialization
            if (instance instanceof InitializingBean){
                ((InitializingBean)instance).afterPropertiesSet();
            }
            // Before initialization
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessAfterInitialization(instance,beanName);
            }
            System.out.println("=========Bean creation completed================" + beanName+"\r\n");
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

