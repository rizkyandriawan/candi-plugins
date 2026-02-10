package candi.auth.core;

import candi.auth.core.widget.CndLoginForm;
import candi.runtime.CandiHandlerMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Auto-configuration for Candi Auth Core.
 * Activates only when a {@link CandiAuthService} bean is present in the application context.
 * Registers the authentication interceptor and imports the login form widget.
 */
@AutoConfiguration
@ConditionalOnBean(CandiAuthService.class)
@Import(CndLoginForm.class)
public class CandiAuthCoreAutoConfiguration implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(CandiAuthCoreAutoConfiguration.class);

    private final CandiAuthService authService;
    private final ApplicationContext applicationContext;

    public CandiAuthCoreAutoConfiguration(CandiAuthService authService, ApplicationContext applicationContext) {
        this.authService = authService;
        this.applicationContext = applicationContext;

        // Register interceptor with CandiHandlerMapping so it applies to Candi page requests.
        // WebMvcConfigurer.addInterceptors() only applies to Spring's built-in handler mappings,
        // not to custom HandlerMapping beans like CandiHandlerMapping.
        try {
            CandiHandlerMapping candiMapping = applicationContext.getBean(CandiHandlerMapping.class);
            candiMapping.addCandiInterceptor(new CandiAuthInterceptor(authService, applicationContext));
            log.info("Candi Auth Core initialized (interceptor registered with CandiHandlerMapping)");
        } catch (Exception e) {
            log.info("Candi Auth Core initialized (CandiHandlerMapping not found, using WebMvc interceptors only)");
        }
    }

    @Bean
    public CandiAuthInterceptor candiAuthInterceptor() {
        return new CandiAuthInterceptor(authService, applicationContext);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // This registers the interceptor with Spring's built-in handler mappings
        // (RequestMappingHandlerMapping, etc.) for non-Candi endpoints like @RestController.
        registry.addInterceptor(candiAuthInterceptor())
                .addPathPatterns("/**");
    }
}
