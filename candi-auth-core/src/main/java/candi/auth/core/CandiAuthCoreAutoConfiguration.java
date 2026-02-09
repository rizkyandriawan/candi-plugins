package candi.auth.core;

import candi.auth.core.widget.CndLoginForm;
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
        log.info("Candi Auth Core initialized");
    }

    @Bean
    public CandiAuthInterceptor candiAuthInterceptor() {
        return new CandiAuthInterceptor(authService, applicationContext);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(candiAuthInterceptor())
                .addPathPatterns("/**");
    }
}
