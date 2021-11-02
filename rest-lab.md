## Asyncronous processing with JAX-RS

1. Create a new payara micro app
2. Open the `RestlabRestApplication` class and change the @ApplicationPath to `@ApplicationPath("/")`.

#### Creating a service to simulate long running processing

1. We will make use of MP Config to externalize for how long we want our service to run. To do so, open the file `resources/META-INF/microprofile-config.properties` and add the following property and default value:

   ```properties
   secondsToWait=5
   ```

2. Add a new service class  `my.company.rest.lab.HelloService`. Annotate it as a @Singleton bean.

3. Add the following code attributes to your service class. This will allow us to log information and to inject external properties with MP Config.

```java
@Singleton
public class HelloService {

    private static Logger LOGGER = Logger.getLogger(HelloService.class.getName());

    @Inject
    @ConfigProperty(name="secondsToWait")
    private Long secondsToWait;

}
```

4. Next, add to the service class the following private method that simply waits for a specific ammount of time:

```java
private void waitForSeconds(Long waitTime) {
	LOGGER.info("We need to wait for "+waitTime+" seconds");
  try {
    TimeUnit.SECONDS.sleep(waitTime);
  } catch (InterruptedException e) {
    throw new RuntimeException(e);
  }
}	
```

5. Finally, let's add the actual long running hello method execution:

```java
    public String processNewHello() {
        LOGGER.info("Starting to slowly process a new hello");
        waitForSeconds(secondsToWait);
        return "Hello processed for "+secondsToWait;
    }
```

Your service class will look like:

```java
@Singleton
public class HelloService {
    private static Logger LOGGER = Logger.getLogger(HelloService.class.getName());

    @Inject
    @ConfigProperty(name="secondsToWait")
    private Long secondsToWait;

    public String processNewHello() {
        LOGGER.info("Starting to slowly process a new hello");
        waitForSeconds(secondsToWait);
        return "Hello processed for "+secondsToWait;
    }

    private void waitForSeconds(Long waitTime) {
        LOGGER.info("We need to wait for "+waitTime+" seconds");
        try {
            TimeUnit.SECONDS.sleep(waitTime);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
```

##### Exposing the HelloService via REST

1. In the `HelloController`, inject the service you've created:

```java
@Inject
HelloService helloService;
```

2. Next, add a new method on the "/slow" path that will invoke your long running execution:

```java
    @GET
    @Path("sync")
    public String slowHelloSync(){
       return helloService.processNewHello();
    }
```

##### Running the application: Sync test

Let's give the application a try. It currently invokes the long running execution method using a synchronous strategy. Let's check that behavior.

1. In the terminal, access your application folder directory. Use maven to package it and then, java to run it. If you want, you can execute both commands in a single line:

   ```
   $ mvn clean package && java -jar target/rest-lab-microbundle.jar
   ```

2. The application will get packaged and the new payara app will start. Once your application starts, make a GET to http://localhost:8080/hello/sync. You can either access via browser, use postman, or run `cURL` in another terminal tab:

   ```
   curl --location --request GET 'http://localhost:8080/hello/sync'
   ```

3. Observe the application log outputs. Which ThreadName can you identify as the thread that is currently executing the `my.company.rest.lab.HelloService`?

4. You should be able to see logs similar to the output below. Notice the "http-thread-pool::http-listener".

   ```
   [2021-11-02T02:16:18.540-0300] [] [INFO] [] [javax.enterprise.system.container.web.com.sun.web.security] [tid: _ThreadID=114 _ThreadName=http-thread-pool::http-listener(2)] [timeMillis: 1635830178540] [levelValue: 800] Context path from ServletContext:  differs from path from bundle: /
   
   [2021-11-02T02:16:23.148-0300] [] [INFO] [] [my.company.rest.lab.HelloService] [tid: _ThreadID=114 _ThreadName=http-thread-pool::http-listener(2)] [timeMillis: 1635830183148] [levelValue: 800] Starting to slowly process a new hello
   
   [2021-11-02T02:16:23.149-0300] [] [INFO] [] [my.company.rest.lab.HelloService] [tid: _ThreadID=114 _ThreadName=http-thread-pool::http-listener(2)] [timeMillis: 1635830183149] [levelValue: 800] We need to wait for 5 seconds
   ```

Now, let's add an asyncronous endpoint to execute the same service method.

#### Creating an async endpoint with @AsyncEvent

1. Open the `HelloController`. Add a new method that we will use to implement the Async execution:

```java
    @GET
    @Path("async")
    public void slowHelloAsync(){

    }
```

2. Next, add the`AsyncResponse` parameter to your method as follows

```java
    public String slowHelloAsync(@Suspended final AsyncResponse asyncResponse){
```

We will need an `java.util.concurrent.ExecutorService` to start our execution in a different thread. Let's create a new producer leveraging CDI capabilities to help us manageding the executor lifecycle. 

3. Add a new class `my.company.rest.lab.ExecutorServiceProducer`:

```java
@ApplicationScoped
class ExecutorServiceProducer {

    private static final int THREADS = 10;

    @Produces
    ExecutorService getExecutorService() {
        return Executors.newFixedThreadPool(THREADS);
    }
}
```

4. Back to our `HelloController`, let's now inject a `java.util.concurrent.ExecutorService`.

```java
    @Inject
    private ExecutorService executorService;
```

5. We can now make use of the ExecutorService to run our method in another thread:

```java
    @GET
    @Path("async")
    public void slowHelloAsync(@Suspended final AsyncResponse asyncResponse){
        executorService.execute(
                new Runnable() {
                    public void run() {
                        String result = helloService.processNewHello();
                        asyncResponse.resume(result);
                    }
                });
    }

```

##### Running the application: Async test

Now, let's package and run our application. We will now invoke the /hello/async endpoint and validate the threads used on the execution.

1. Package and run your app:

   ```
   mvn clean package && java -jar target/rest-lab-microbundle.jar
   ```

2. Once it starts, execute a GET to http://localhost:8080/hello/async. You can either access via browser, use postman, or run `cURL` in another terminal tab:

   ```
   curl --location --request GET 'http://localhost:8080/hello/async'
   ```

3. Observe the application logs output. Did the `my.company.rest.lab.HelloService` execution run in the same thread as before?
4. If you check the logs, you should be able to see a log output like the one below. Notice that the execution of `my.company.rest.lab.HelloService` was not executed by the `http-thread-pool::http-listener` thread anymore. Instead, it was executed by `pool-27-thread-1`. (*Names can vary*)

```
[2021-11-02T02:33:15.715-0300] [] [INFO] [] [javax.enterprise.system.container.web.com.sun.web.security] [tid: _ThreadID=114 _ThreadName=http-thread-pool::http-listener(2)] [timeMillis: 1635831195715] [levelValue: 800] Context path from ServletContext:  differs from path from bundle: /

[2021-11-02T02:33:15.769-0300] [] [INFO] [] [my.company.rest.lab.HelloService] [tid: _ThreadID=143 _ThreadName=pool-27-thread-1] [timeMillis: 1635831195769] [levelValue: 800] Starting to slowly process a new hello

[2021-11-02T02:33:15.769-0300] [] [INFO] [] [my.company.rest.lab.HelloService] [tid: _ThreadID=143 _ThreadName=pool-27-thread-1] [timeMillis: 1635831195769] [levelValue: 800] We need to wait for 5 seconds
```

#### Setting a timeout

It is a good practice to set a timeout to our async endpoint so that the connection is not open for longer than it is expected. In this case, we will use a `TimeoutHandler` to make sure we don't go over a 2 seconds execution.

1. Open your `HelloController`. In the begining of the method `slowHelloAsync`, add the handling logic:

```java
        asyncResponse.setTimeoutHandler(new TimeoutHandler() {
            public void handleTimeout(AsyncResponse callback) {
                callback.resume(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("Operation timed out!!").build());
            }
        });
        asyncResponse.setTimeout(2, TimeUnit.SECONDS);
```

2. The `slowHelloAsync` method should now look similar to:

```java
    @GET
    @Path("async")
    public void slowHelloAsync(@Suspended final AsyncResponse asyncResponse){
        asyncResponse.setTimeoutHandler(new TimeoutHandler() {
            public void handleTimeout(AsyncResponse callback) {
                callback.resume(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("Operation timed out!!").build());
            }
        });
        asyncResponse.setTimeout(2, TimeUnit.SECONDS);

        executorService.execute(
                new Runnable() {
                    public void run() {
                        String result = helloService.processNewHello();
                        asyncResponse.resume(result);
                    }
                });
    }
```

#### Testing the timeout handling

Let's package our application so we can try out out timeout handling.

1. Use maven to package your app:

```
mvn clean package
```

2. Start the application with the default waiting time, 5s.

```
java -jar target/rest-lab-microbundle.jar
```

3. Once it up, execute the GET request to http://localhost:8080/hello/async. You can either access via browser, use postman, or run `cURL` in another terminal tab:

   ```
   curl --location --request GET 'http://localhost:8080/hello/async'
   ```

4. Observe the application logs output. Did the `my.company.rest.lab.HelloService` log an execution of five seconds? And what was the response to your request?

4. If you try accessing your endpoint, you should get a timeout after two seconds! Similar to:

```
$ curl --location --request GET 'http://localhost:8080/hello/async'
Operation timed out!!
```

6. Now, stop your application.
7. Now, our long running code will only take one second to execute. Start it with the following command:

```
java -jar -DsecondsToWait=1 target/rest-lab-microbundle.jar
```

8. Run the same GET request to http://localhost:8080/hello/async. What happened? Did you get a timeout or successful execution?

-------

### Congratulations!

You have sucessfully created a new JAX-RS application from scratch using Payara Micro. You used CDI to produce beans and to create singleton instances in your app. You could also adopt good practices of externalizing your application configuration via MicroProfile Config. Finally, you could explore the different ways the threads behave when you use synchronous vs asynchronous processing in the REST endpoints in server-side.
