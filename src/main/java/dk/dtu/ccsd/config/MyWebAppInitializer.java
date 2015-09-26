package dk.dtu.ccsd.config;

/**
 * Created by xiuli on 3/2/15.
 */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import java.util.Set;

public class MyWebAppInitializer implements WebApplicationInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(MyWebAppInitializer.class);

    @Override
    public void onStartup(ServletContext servletContext) {
        // Create the 'root' Spring application context
        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
        // Register application config
        rootContext.register(AppConfig.class);
        rootContext.refresh();
        // Manage the lifecycle of the root application context
        servletContext.addListener(new ContextLoaderListener(rootContext));
        servletContext.setInitParameter("defaultHtmlEscape", "true");



        // Register and map the dispatcher servlet
        AnnotationConfigWebApplicationContext mvcContext = new AnnotationConfigWebApplicationContext();
        mvcContext.register(MvcConfig.class);
        addServiceDispatcherServlet(servletContext, mvcContext);

        // Register Encoding Filter
        // addEncodingFilter(servletContext);

        // Register Logging Filter
        // addLoggingFilter(servletContext);
    }

    private void addServiceDispatcherServlet(ServletContext container, AnnotationConfigWebApplicationContext mvcContext) {
        final String SERVICES_MAPPING = "/api/*";

        ServletRegistration.Dynamic dispatcher = container.addServlet("dispatcher", new DispatcherServlet(mvcContext));
        dispatcher.setLoadOnStartup(1);
        Set<String> mappingConflicts = dispatcher.addMapping(SERVICES_MAPPING);

        if (!mappingConflicts.isEmpty()) {
            for (String s : mappingConflicts) {
                LOG.error("Mapping conflict: " + s);
            }
            throw new IllegalStateException("'ServicesDispatcher' could not be mapped to '" + SERVICES_MAPPING + "'");
        }
    }

    private void addEncodingFilter(ServletContext container) {
        FilterRegistration.Dynamic fr = container.addFilter("encodingFilter", new CharacterEncodingFilter());
        fr.setInitParameter("encoding", "UTF-8");
        fr.setInitParameter("forceEncoding", "true");
        fr.addMappingForUrlPatterns(null, true, "/*");
    }

    private void addLoggingFilter(ServletContext container) {
        FilterRegistration.Dynamic fr = container.addFilter("loggingFilter", new CommonsRequestLoggingFilter());
        fr.addMappingForUrlPatterns(null, true, "/*");
    }

}