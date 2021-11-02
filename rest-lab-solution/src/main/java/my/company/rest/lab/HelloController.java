package my.company.rest.lab;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.container.TimeoutHandler;
import javax.ws.rs.core.Response;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 */
@Path("/hello")
@Singleton
public class HelloController {

    @Inject
    HelloService helloService;

    @Inject
    private ExecutorService executorService;

    @GET
    public String sayHello() {
        return "Hello World";
    }

    @GET
    @Path("sync")
    public String slowHelloSync(){
       return helloService.processNewHello();
    }

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
}
