package my.company.cdi.demo.extension;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 *  This will be used as the implementation of a Portable Extension.
 *  TODO: This class should implement the javax.enterprise.inject.spi.Extension interface.
 */
public class StartupBeanExtension implements javax.enterprise.inject.spi.Extension {

    private static final Logger LOGGER = Logger.getLogger(StartupBeanExtension.class.getName());

    // Will store all the beans annotated with @StartUp
    private final Set<Bean<?>> startupBeans = new LinkedHashSet<>();

    // Will store all the beans intercepted during the ProcessBean event, part of the the CDI Lifecycle.
    private final Set<Bean<?>> allProcessedBeans = new LinkedHashSet<>();

    /**
     * TODO: Implement a method that @observes every ProcessBean event that fires.
     * To know more about the ProcessBean event and other CDI lifecycle events, check:
     * https://jakarta.ee/specifications/cdi/2.0/cdi-spec-2.0.html#process_bean
     */
    private void processBean(@Observes ProcessBean event) {
        Annotated annotated = event.getAnnotated();

        /**
         * TODO: Fix the conditional below. The list startupBeans can only include a bean if it is annotated with @StartUp and if it is set to ApplicationScope.
         * TIP: the method isStartUpBean can help you defining if the intercepted Annotated bean meets the description above;
          */
        if (isStartUpBean(annotated)) {
            LOGGER.info("===> New bean with @StartUp found: " + event.getBean());

            /**
             * TODO: Obtain the bean from the event object, and add it to the startupBeans list.
             */
            startupBeans.add(event.getBean());
        }

        // Adds every processed bean to the list of processed beans when a ProcessBean event was fired.
        allProcessedBeans.add(event.getBean());
    }

    private boolean isStartUpBean(Annotated annotated) {
        return annotated.isAnnotationPresent(StartUp.class) && annotated.isAnnotationPresent(ApplicationScoped.class);
    }

    /**
     * TODO: Fix the method declaration. This method should observe every AfterDeploymentValidation event fired.
     * TIP: This method goal is to eagerly initialize the bean. In this way, whenever the instance is injected it is already created and part of the CDI context.
     */
    void afterDeploymentValidation(@Observes AfterDeploymentValidation event, BeanManager manager) {
        LOGGER.info("===> Number of classes with @StartUp: " + startupBeans.size()+", Total number of intercepted beans at ProcessBean phase: " + allProcessedBeans.size());

        // Initialize all beans annotated with @StartUp
        for (Bean<?> bean : startupBeans) {
            // Use to toString() "Workaround" as a way to initialize every reference of the beans annotated with @StartUp
            manager.getReference(bean, bean.getBeanClass(), manager.createCreationalContext(bean)).toString();
        }
    }
}