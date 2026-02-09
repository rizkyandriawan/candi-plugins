package candi.auth.classic;

import candi.auth.core.CandiAuthService;
import candi.auth.core.CandiUserProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * Auto-configuration for classic session-based Candi authentication.
 * Activates when Spring Security's {@link SecurityContext} is on the classpath
 * and a {@link CandiUserProvider} bean is present.
 */
@AutoConfiguration
@ConditionalOnClass(SecurityContext.class)
@ConditionalOnBean(CandiUserProvider.class)
@EnableConfigurationProperties(CandiAuthClassicProperties.class)
@Import(ClassicSecurityConfig.class)
public class CandiAuthClassicAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(CandiAuthClassicAutoConfiguration.class);

    public CandiAuthClassicAutoConfiguration() {
        log.info("Candi Auth Classic (session-based) initialized");
    }

    @Bean
    public UserDetailsService candiUserDetailsService(CandiUserProvider userProvider) {
        return new ClassicUserDetailsService(userProvider);
    }

    @Bean
    public CandiAuthService candiAuthService(AuthenticationManager authenticationManager) {
        return new ClassicCandiAuthService(authenticationManager);
    }
}
