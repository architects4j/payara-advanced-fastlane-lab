package my.company.cdi.demo;

import my.company.cdi.demo.load.NameLoaderStartup;

import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;
import java.util.Set;
import java.util.function.Supplier;

public class App {

    public static void main(String[] args) {
        //TODO Initialize the SeContainer
        try (SeContainer container = SeContainerInitializer.newInstance().initialize()) {
            System.out.println("Starting the NameLoaderStartup class");
            Supplier<Set<String>> loaderStartup = container.select(NameLoaderStartup.class).get();

            System.out.println("The fakes names" + loaderStartup.get());
        }
    }
}
