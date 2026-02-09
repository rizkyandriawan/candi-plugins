package candi.auth.jwt;

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
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Auto-configuration for stateless JWT-based Candi authentication.
 * Activates when JJWT is on the classpath and a {@link CandiUserProvider} bean is present.
 */
@AutoConfiguration
@ConditionalOnClass(io.jsonwebtoken.Jwts.class)
@ConditionalOnBean(CandiUserProvider.class)
@EnableConfigurationProperties(CandiAuthJwtProperties.class)
@Import(JwtSecurityConfig.class)
public class CandiAuthJwtAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(CandiAuthJwtAutoConfiguration.class);

    public CandiAuthJwtAutoConfiguration() {
        log.info("Candi Auth JWT (stateless) initialized");
    }

    @Bean
    public JwtTokenService jwtTokenService(CandiAuthJwtProperties properties) {
        return new JwtTokenService(properties);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenService tokenService,
                                                           CandiAuthJwtProperties properties) {
        return new JwtAuthenticationFilter(tokenService, properties.getCookieName());
    }

    @Bean
    public CandiAuthService candiAuthService(JwtTokenService tokenService,
                                             CandiUserProvider userProvider,
                                             PasswordEncoder passwordEncoder,
                                             CandiAuthJwtProperties properties) {
        return new JwtCandiAuthService(tokenService, userProvider, passwordEncoder, properties);
    }
}
