package com.example.music.service.cdi.extension;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

//TODO: CDI Portable Extensions should implement the javax.enterprise.inject.spi.Extension interface
public class StartupBeanExtension {

    private static final Logger LOGGER = Logger.getLogger(StartupBeanExtension.class.getName());

    private final Set<Bean<?>> startupBeans = new LinkedHashSet<>();

    //TODO: Should @Observes ProcessBean event
    <X> void processBean(ProcessBean<X> event) {
        Annotated annotated = event.getAnnotated();
        if (isStartUpBean(annotated)) {
            LOGGER.info("New StartUp class found: " + event.getBean());
            startupBeans.add(event.getBean());
        }
    }

    private boolean isStartUpBean(Annotated annotated) {
        return annotated.isAnnotationPresent(StartUp.class)
                && annotated.isAnnotationPresent(ApplicationScoped.class);
    }

    //TODO: Should @Observes AfterDeploymentValidation event
    void afterDeploymentValidation(AfterDeploymentValidation event, BeanManager manager) {
        LOGGER.info("Number of StartUp classes: " + startupBeans.size());
        for (Bean<?> bean : startupBeans) {
            // the call to toString() is a cheat to force the bean to be initialized
            manager.getReference(bean, bean.getBeanClass(), manager.createCreationalContext(bean)).toString();
        }
    }
}