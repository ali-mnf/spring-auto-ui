package com.mnf.springautoui;

import com.mnf.springautoui.controller.AutoUIController;
import com.mnf.springautoui.util.detection.ControllerMethodDetector;
import com.mnf.springautoui.util.propertiesConfig.PropertiesService;
import jakarta.servlet.ServletContext;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.DispatcherServlet;

@AutoConfiguration
@ConditionalOnClass(DispatcherServlet.class)
public class AutoUiAutoConfiguration {

    /** Your detector has a ctor: (ListableBeanFactory, PropertiesService, ServletContext) */
    @Bean
    @ConditionalOnMissingBean
    public ControllerMethodDetector controllerMethodDetector(
            ListableBeanFactory listableBeanFactory,
            PropertiesService propertiesService,
            ServletContext servletContext
    ) {
        return new ControllerMethodDetector(listableBeanFactory, propertiesService, servletContext);
    }

    /** Your controller has a ctor: (ControllerMethodDetector, ServletContext) */
    @Bean
    @ConditionalOnMissingBean
    public AutoUIController autoUIController(
            ControllerMethodDetector detector,
            ServletContext servletContext
    ) {
        return new AutoUIController(detector, servletContext);
    }
}
