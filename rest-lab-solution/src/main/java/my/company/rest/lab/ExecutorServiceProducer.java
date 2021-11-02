package my.company.rest.lab;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApplicationScoped
class ExecutorServiceProducer {

    private static final int THREADS = 10;

    @Produces
    ExecutorService getExecutorService() {
        return Executors.newFixedThreadPool(THREADS);
    }
}