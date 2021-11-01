package my.company.cdi.demo.load;

import com.github.javafaker.DragonBall;
import com.github.javafaker.Faker;
import my.company.cdi.demo.extension.StartUp;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Logger;

// TODO Annotate this bean with ApplicationScoped and StartUp

public class NameLoaderStartup implements Supplier<Set<String>> {

    private static final Logger LOGGER = Logger.getLogger(NameLoaderStartup.class.getName());

    private final Set<String> names = new HashSet<>();

    @PostConstruct
    public void onStartup() {
        LOGGER.info("it will load eagerly");

        try {
            TimeUnit.SECONDS.sleep(2L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Faker faker = new Faker();
        DragonBall dragonBall = faker.dragonBall();
        for (int index = 0; index < 15; index++) {
            this.names.add(dragonBall.character());
        }
    }

    @Override
    public Set<String> get() {
        return Collections.unmodifiableSet(names);
    }
}