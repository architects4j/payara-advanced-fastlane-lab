# Creating a CDI Portable Extension

In order to seamlessly add extra behavior to your application you can leverage CDI powerful portable extension. With this capability, you can observe the CDI lifecycle events and add your custom code.

In this hands-on exercise, you will create a new CDI portable extension that can eagerly initialize specific beans, more specifically, only beans annotated with a custom annotation. 

## Setup

To get started, clone the following repository to your development environment:

```
git clone https://github.com/otaviojava/payara-advanced-fastlane-lab
```

A new folder named `payara-advanced-fastlane-lab` will be created. In this folder you will find the project `cdi-lab`.

Open the project `payara-advanced-fastlane-lab/cdi-lab` in your favorite IDE for Java development. 

## Use case

In this example, you're required to fix a broken application. Your development team asked for an example on how they can achieve the following goals:

- Have a unique instance of a specific class in the application scope, so that, every time the class is injected, the same object instance will be used.
- As a best practice, the team wants to be able to annotate the beans with @StartUp every time they need the behavior above.
- The team asked not only the implementation of a CDI Portable Extension, but also, for an example of a bean that initializes a Set<String> as soon as the application starts. **Eagerly** (when injected, it should already be instatiated).

### Creating your CDI Portable Extension

##### The @StartUp annotation

1. Open the StartUp annotation declaration on package my.company.cdi.demo.extension. This is the annotation that should be used on the beans you will initialize when the application starts. This annotation is good to go and you don't need to change it.

##### The Extension implementation

1. Open the `StartUpBeanExtension` class.

2. Make sure it `implements javax.enterprise.inject.spi.Extension` on line 14. It should look like:

   ````java
   public class StartupBeanExtension implements javax.enterprise.inject.spi.Extension 
   ````

3. Open the configuration file `src/main/resources/META-INF/services/javax.enterprise.inject.spi.Extension`. Add the fully qualified name of your extension implementation, the StartupBeanExtension.

   ```properties
   #TODO: Add the FQDN of your CDI portable extension here.
   my.company.cdi.demo.extension.StartupBeanExtension
   ```

**Observing CDI Events**

Let's now start implementing some code on our `StartUpBeanExtension` extension class so that it starts reacting to CDI Lifecycle events. We'll write some code to be executed when a `ProcessBean` event is fired, then, we'll implement some logic when an `AfterDeploymentValidation` event is fired. 

To get started, let's work on the two lists we will use to store beans. 

| :information_source:  | TIP: Since Java 5 we have generics as part of Java. It's a good practice to specify the actual type we expect in our collection, and using Generics will help us doing this at compile time. |
| ------------------------- | ------------------------------------------------------------ |

We'll use these two sets to do custom logic but also to understand the behavior of our code. But first, let's adopt some good practices.

1. On lines 19 and 22, we have two lists (`java.util.Set`) in our application: startupBeans and allProcessedBeans. In this lists we will store the beans that we will intercept in the events fired in CDI lifecycle. The sets will store objects that are managed beans. Therefore, adjust both Set to store Bean<?>. It will look like:

   ```java
       // Will store all the Bean<?> annotated with @StartUp
       private final Set<Bean<?>> startupBeans = new LinkedHashSet<>();
   
       // Will store all the Bean<?> intercepted during the ProcessBean event, part of the the CDI Lifecycle.
       private final Set<Bean<?>> allProcessedBeans = new LinkedHashSet<>();
   ```

**Reacting to ProcessBean events**

1. Check the method processBean on line 29. When this method was created the goal was for it to actually execute every time a new `ProcessBean` event fired in the CDI context. The team created it correctly by adding parameter `event` with type `ProcessBean`. Although, there is still an issue in this method declaration. 


:bangbang: | Try to spot the issue by yourself before moving to the next step. 
:---: | :---   

3. In order to help in this, [CDI offers eventing capabilities](https://jakarta.ee/specifications/cdi/2.0/cdi-spec-2.0.html#events). This method is not an observer of the `ProcessBean` events. In order to fix it, add the @observes annotation as follows: 

   ```java
       private void processBean(@Observes ProcessBean event) {
   ```

4. In this method we want to achieve two goals: 
   1. For every event fired, store the bean in the `allProcessedBeans` set; 
   2. For every event fired, store ONLY beans annotated with `@StartUp` and `@ApplicationScoped` on the `startupBeans` set.

5. To achieve these goals: 

   1. Fix the `if (true)` statement on line 36; (You can use the existing `isStartUpBean` method to help you filter the Beans.)

   2. Inside the if statement, get the bean in the `event` object and addit to the `startupBeans` set.

   3. After the if statement, get the bean in the `event` object and add it to the `allProcessedBeans` set; 

:bangbang: | Try to implement this method yourself before moving to the next topics. 
:---: | :---   

6. To test if our annotated bean is actually a StartUp bean, we can fix the if with a code similar to:
```java
if (isStartUpBean(annotated)) {
```

6. Next, once we identified the StartUp beans, we can add it to the list: 

```java
            /**
             * TODO: Obtain the bean from the event object, and add it to the startupBeans list.
             */
             startupBeans.add(event.getBean());
```

7. And, we can add all beans to the right set:

```java
        // Adds every processed bean to the list of processed beans when a ProcessBean event was fired.
        allProcessedBeans.add(event.getBean());
```

#### Using the @StartUp annotation and the new extension

To test what we did so far, let's configure new bean to use the @StartUp annotation. This bean will initialize a list of characters names. Then, we will create a new SeContainer, inject this managed bean and output the list. While we do this, we'll observe how our extension works and when each behavior is triggered by CDI.

1. Open the `my.company.cdi.demo.App` class. On line 14, notice the CDI container `SeContainer` is not correctly initialized. Use the `SeContainerInitializer` to initialize it properly.

   ```java
           try (SeContainer container = SeContainerInitializer.newInstance().initialize()) {
   ```

2. Notice that the code is correctly injecting a `NameLoaderStartup`. Then it lists all the names in the `Set<String> names` attribute, part of `NameLoaderStartup` class. This code in `App` is correct and needs no further adjustments.

3. Now, open the `my.company.cdi.demo.load.NameLoaderStartup`. This bean will make use of the extended functionality we just implemented in the extension. To make use of our extension, it needs to be application scopped, and should also be annotated with @StartUp. On line 17, add both annotations.

   ```java
   @ApplicationScoped
   @StartUp
   public class NameLoaderStartup implements Supplier<Set<String>> {
   ```
   
4. Explore this class and its methods. Notice in the `onStartup` method it initializes the `names` set by using an external library called Faker. This library is used for learning purposes only.

5. Run the `App` class. Take a look at the log output.

   1. How many StartUp beans did your extension identify?
   2. How many beans did you store in your list when you did not filter the exact bean type you wanted?
   3. Do you think the bean `NameLoaderStartup` was initialized during the application initialization or only when you injected it for the first time? (Eager or lazy bean initialization?)


Here's a log output that should be similar to the expected output at this point:

```
Nov 01, 2021 7:37:23 PM org.jboss.weld.environment.se.WeldContainer fireContainerInitializedEvent
INFO: WELD-ENV-002003: Weld SE container bf5506d1-9c7b-4410-bcb5-75ec449073eb initialized
Starting the NameLoaderStartup class
Nov 01, 2021 7:37:23 PM my.company.cdi.demo.load.NameLoaderStartup onStartup
INFO: it will load eagerly
The fakes names[Super Saiyan Goten, Super Saiyan 2 Goku, Majin Vegeta, Super Saiyan 2 Vegeta, King Vegeta, Super Saiyan 2 Gohan, Su Shenlong, Android 18, Bardock, Hit, Nail, King Cold, Captain Ginyu, San Shenlong, Super Saiyan Goku]
Nov 01, 2021 7:37:26 PM org.jboss.weld.environment.se.WeldContainer shutdown
INFO: WELD-ENV-002001: Weld SE container bf5506d1-9c7b-4410-bcb5-75ec449073eb shut down
```

##### Eager Bean Initialization

Open the extension and let's make sure our StartUp beans are eagerly initialized. 

1. To initialize the StartUp beans we identified and stored on the set previously, let's add some behavior during the `AfterDeploymentValidation` phase. Locate the method `afterDeploymentValidation`, that is partially implemented.
2. There is an error on the method declaration. **Can you spot it?**
3. This method should observe every `AfterDeploymentValidation` events but it is currently reacting to `AfterBeanDiscovery` events. Fix it so it looks like:

```java
void afterDeploymentValidation(@Observes AfterDeploymentValidation event, BeanManager manager) 
```

4. Since you already adopted good practices on the Set `startupBeans` declaration, adjust your iteration with `for` accordingly:

```java
for (Bean<?> bean : startupBeans) {
```

5. On line 62, there is a commented code:

   ```java
   //            manager.getReference(bean, bean.getBeanClass(), manager.createCreationalContext(bean)).toString();
   ```

6. What do you think this code does? 

7. Uncomment this code, and run the `App` class again. Observe the log output. If you couldn't spot differences, try commenting it again, running the app and comparing.
8. What changed? Why?

Here's an example of expected output at this point:

```
Nov 01, 2021 7:39:04 PM org.jboss.weld.bootstrap.WeldStartup <clinit>
INFO: WELD-000900: 3.1.8 (Final)
Nov 01, 2021 7:39:04 PM org.jboss.weld.bootstrap.WeldStartup startContainer
INFO: WELD-000101: Transactional services not available. Injection of @Inject UserTransaction not available. Transactional observers will be invoked synchronously.
Nov 01, 2021 7:39:04 PM my.company.cdi.demo.extension.StartupBeanExtension processBean
INFO: ===> New bean with @StartUp found: Managed Bean [class my.company.cdi.demo.load.NameLoaderStartup] with qualifiers [@Any @Default]
Nov 01, 2021 7:39:04 PM my.company.cdi.demo.extension.StartupBeanExtension afterDeploymentValidation
INFO: ===> Number of classes with @StartUp: 1, Total number of intercepted beans at ProcessBean phase: 50
Nov 01, 2021 7:39:04 PM my.company.cdi.demo.load.NameLoaderStartup onStartup
INFO: it will load eagerly
Nov 01, 2021 7:39:06 PM org.jboss.weld.environment.se.WeldContainer fireContainerInitializedEvent
INFO: WELD-ENV-002003: Weld SE container ca4577b8-0120-48b1-8e3b-6062ed803d7f initialized
Nov 01, 2021 7:39:06 PM org.jboss.weld.environment.se.WeldContainer shutdown
INFO: WELD-ENV-002001: Weld SE container ca4577b8-0120-48b1-8e3b-6062ed803d7f shut down
Starting the NameLoaderStartup class
The fakes names[Super Saiyan Trunks, Oolong, Majin Vegeta, Garlic Jr, Super Saiyan Blue Vegeta, Super Saiyan 2 Gohan, Vegito, Su Shenlong, Android 18, Super Saiyan Vegeta, Goku, Vados, Mystic Gohan, Freeza, Super Saiyan Goku]
```

## Congratulations

You have successfully created a Portable Extension that reacts to CDI Lifecycle events! You also created a managed bean that uses a new annotation and that is eagerly initialized. Finally, you could also check and validate the different behaviors of the different components by analyzing Weld logs.
