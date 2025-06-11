# Handwritten Spring Framework

A simple handwritten Dependency Injection (DI) framework that mimics Spring, designed to help understand the underlying principles of Spring.
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/Levio-z/hand-writting-spring)
This handwritten Spring framework implements several classic design patterns. Based on code analysis, the following design patterns are mainly used:

Factory Pattern
The SleepApplicationContext class acts as a Bean factory, responsible for creating and managing all Bean instances. SleepApplicationContext.java:123-127

The createBean() method is the core factory method, creating Bean instances based on BeanDefinition metadata. SleepApplicationContext.java:123-164

Singleton Pattern
The framework implements singleton Bean management through the singletonObjects map. SleepApplicationContext.java:20

In the getBean() method, singleton Beans are created only once and reused from the cache. SleepApplicationContext.java:106-111

Template Method Pattern
Bean lifecycle management follows a fixed template process: instantiation → dependency injection → Aware callback → pre-initialization processing → custom initialization → post-initialization processing. SleepApplicationContext.java:135-151

Observer Pattern
The BeanPostProcessor interface implements the observer pattern, allowing multiple processors to listen to Bean lifecycle events. BeanPostProcessor.java:10-12

For example, SleepBeanPostProcessor implements this interface and executes custom logic before and after Bean initialization. SleepBeanPostProcessor.java:11-18

Proxy Pattern
In SleepBeanPostProcessor, JDK dynamic proxies are used to create proxy objects for Beans, implementing AOP functionality. SleepBeanPostProcessor.java:25-33

Dependency Injection Pattern
Dependency injection is implemented through the @Autowired annotation and reflection mechanism, decoupling dependencies between Beans. SleepApplicationContext.java:128-134

## Table of Contents

1. [Code Structure](#code-structure)
2. [Implementation Entry](#implementation-entry)
3. [Annotations](#annotations)
   - [Scanning](#scanning)
   - [Bean Annotation](#bean-annotation)
   - [Singleton Annotation](#singleton-annotation)
   - [Autowired Annotation](#autowired-annotation)
4. [Container Class](#container-class)
   - [Data Structures](#data-structures)
   - [Methods](#methods)
5. [Other Data Structures](#other-data-structures)
6. [Spring AOP](#spring-aop)

## Code Structure

```
├─src
│  ├─main
│  │  ├─java
│  │  │  └─com
│  │  │      ├─sleep
│  │  │      │  └─service
│  │  │      └─spring
```

## Implementation Entry

In the `Test` class, we initialize the application context using `SleepApplicationContext` and retrieve the `userService` bean.

```java
public class Test {  
    public static void main(String[] args) {  
        SleepApplicationContext applicationContext = new SleepApplicationContext(AppConfig.class);  
        UserInterface userInterface = (UserInterface)applicationContext.getBean("userService");  
        System.out.println(userInterface);  
        userInterface.test();  
    }  
}
```

- `SleepApplicationContext applicationContext = new SleepApplicationContext(AppConfig.class);`  
  - Initializes the application context and loads the configuration class.
- `UserInterface userInterface = (UserInterface)applicationContext.getBean("userService");` 
  - Retrieves the `userService` bean from the context.

## Annotations

### Scanning

Specify the package path to scan using the `@ComponetScan` annotation.

Package path: sleep

```java
@ComponetScan("com/sleep/service")
public class AppConfig {
}
```

- Scanning path: com/sleep/service

Package path: spring

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ComponetScan {
    String value();
}
```

- Runtime
- Can only be written on classes
- Attributes
  - value: Scanning path

### Bean Annotation

Define a bean using the `@Component` annotation.

```java
@Component("orderService")
public class OrderService {
}
```

- Manages a bean named orderService

### Singleton Annotation

Specify the scope of a bean using the `@Scope` annotation.

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Scope {
    String value() default "singleton" ;
}
```

### Autowired Annotation

Implement dependency injection using the `@Autowired` annotation.

```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD,ElementType.METHOD})
public @interface Autowired {
}
```

## Container Class

### Data Structures

Defines the structures for storing singleton beans and bean definitions in the container class.

```java
// Container for singleton beans
private ConcurrentHashMap<String,Object> singletonObjects = new ConcurrentHashMap<>();
// Bean definitions
private ConcurrentHashMap<String,BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
//
private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();
```

### Methods

#### Configuration Parsing Method

Parses the configuration class in the `SleepApplicationContext` constructor, scans, and creates singleton beans.

```java
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
```

- `scan(configClass);`
  - Parses the `@ComponetScan` annotation and scans the specified path.
- Creates singleton objects and puts them into the object pool.

#### Scanning Method: scan

Scans the classes under the specified package path and checks for the `@Component` annotation.

```java
private void scan(Class configClass) {
    // Parse the configuration class
    // Parse this annotation-->scanning path-->then scan @ComponetScan("com.sleep.service")
    ComponetScan componetScanAnnotation = (ComponetScan) configClass.getDeclaredAnnotation(ComponetScan.class);
    // Get the attributes of the annotation
    String path = componetScanAnnotation.value();
    // Print the path
    System.out.println("path:"+path);
    // Scan
    // 1. Get all classes based on the package name of the scanning path
    // 2. Bootstrap-->jre/lib
    // 3. Ext-->jre/extt/lib
    // 4. App-->classpath
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
                        // Parse class-->BeanDefinition
                        // Determine whether the current bean is a singleton or prototype
                        // BeanDefinition
                        // Determine whether the class implements the interface
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
```

- Get the class path in ComponetScan

```java
ClassLoader classLoader = SleepApplicationContext.class.getClassLoader();
URL resource = classLoader.getResource(path);
File file = new File(resource.getFile());
```

- Use the class loader to get resources, convert resource to File, and traverse all files

```java
if (file.isDirectory()){
    File[] files = file.listFiles();
    for (File f:files){
        ...
    }
}
```

- Process string: windows file name->com.service class path

```java
if (absolutePath.endsWith(".class")){
    String className = absolutePath.substring(absolutePath.indexOf("com"), absolutePath.indexOf(".class"));
    String replace = className.replace("\\", ".");
    System.out.println("absolutePath:"+absolutePath+"\nreplace:"+replace);
```

- Parsing

```java
try {
    Class<?> clazz = classLoader.loadClass(replace);
    if (clazz.isAnnotationPresent(Component.class)) {}
}
```

- .loadClass: Load class by class name
- Check if there is a Component annotation

```java
// Generate bean object
// Parse class-->BeanDefinition
// Determine whether the current bean is a singleton or prototype
// BeanDefinition
// Determine whether the class implements the interface
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
```

- Initialize BeanDefinition Bean definition object
  - Get the bean annotation and name
  - Set whether it is a singleton or prototype
  - Add to the BeanDefinitionMap container
- During scanning, it will also check if it implements the BeanPostProcessor interface

```java
// Determine whether the class implements the interface
if (BeanPostProcessor.class.isAssignableFrom(clazz)){
    BeanPostProcessor o = (BeanPostProcessor)clazz.getDeclaredConstructor().newInstance();
    beanPostProcessorList.add(o);
}
```

- If so, create an object with clazz, cast it to a BeanPostProcessor instance, and add it to the BeanPostProcessor object container pool

#### Get Bean Method

Get the specified bean by name using the `getBean` method.

```java
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
```

- BeanDefinitionMap checks if there is an object
  - Is it a prototype or singleton
    - Prototype gets the previously created object
    - Singleton creates a new object

#### Create Bean Method

Create a bean instance and perform dependency injection using the `createBean` method.

```java
public Object createBean(BeanDefinition beanDefinition,String beanName){
    Class clazz = beanDefinition.getClazz();
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
        // P7.Aware callback
        if (instance instanceof BeanNameAware){
            ((BeanNameAware)instance).setBeanName(beanName);
        }
        // After initialization
        // Can add digital sorting
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
```

- Get type: Class clazz = beanDefinition.getClazz();
- Create object through reflection: Object instance = clazz.getDeclaredConstructor().newInstance();
- Dependency injection method:
  - For each declared field
  - Has Autowired
    - Get bean object
      - Object bean = getBean(declaredField.getName());
    - Set field to accessible
    - declaredField.set(instance, bean); Set field to bean

- Callback
  - Check if it implements BeanNameAware
  - Cast type, class can receive beanname
- Self-controlled initialization
  - Whether to implement: InitializingBean
  - Cast type, call related methods
- BeanPostProcessor extension mechanism: Bean post processor
  - Before initialization
    - Cast bean object
    - Pass in each object's instance and bean name
  - After initialization
  - In spring, calling getBean can get attributes

### Implement a Bean to Extend userService with BeanPostProcessor

Extend the functionality of userService by implementing the `BeanPostProcessor` interface.

```java
@Component("sleepBeanPostProcessor")
public class SleepBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        System.out.println("Before Initialization");
        if (beanName.equals("userService")){
            ((UserService)bean).setBeanName("Sleeping is great");
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("After Initialization");
        if (beanName.equals("userService")){
            Object proxyInstance = Proxy.newProxyInstance(SleepBeanPostProcessor.class.getClassLoader(), bean.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    System.out.println("Proxy Logic");
                    // Find the cut point
                    return method.invoke(bean,args);
                }
            });
            return proxyInstance;
        }
        return bean;
    }
}
```

- Before initialization:
  - Reset bean name

- Create a new proxy object after initialization
  - After setting, it will print: Proxy Logic before executing the method

## Other Data Structures

### Bean Definition Object

Defines the basic information of a bean, including class, scope, and name.

```java
public class BeanDefinition {
    private Class clazz;
    private String scope;
    private String beanName;
}
```

- Class object
- Prototype
- Name

### BeanNameAware Interface

Classes that implement this interface can get the name of the bean.

```java
public interface BeanNameAware {
    void setBeanName(String beanName);
}
```

- Get the name

### InitializingBean

Classes that implement this interface can perform specific operations after properties are set.

```java
public interface InitializingBean {
    void afterPropertiesSet() throws Exception;
}
```

- What to do after setting the object

### BeanPostProcessor

Defines the bean post processor interface.

```java
public interface BeanPostProcessor {
    Object postProcessBeforeInitialization(Object bean, String beanName);
    Object postProcessAfterInitialization(Object bean, String beanName);
}
```

## Spring AOP

Implement AOP functionality using `BeanPostProcessor`, the key is to create a proxy object after the object is initialized, so that the proxy logic is executed before the business logic when the method is called.

