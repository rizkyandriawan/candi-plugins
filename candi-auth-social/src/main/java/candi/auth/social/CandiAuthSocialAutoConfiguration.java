package candi.auth.social;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration for Candi OAuth2 social login.
 * Activates when at least one social provider is configured via
 * {@code candi.auth.social.providers} properties.
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "candi.auth.social", name = "providers")
@EnableConfigurationProperties(SocialAuthProperties.class)
@Import(SocialAuthConfigurer.class)
public class CandiAuthSocialAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(CandiAuthSocialAutoConfiguration.class);

    public CandiAuthSocialAutoConfiguration() {
        log.info("Candi Auth Social (OAuth2) initialized");
    }

    /**
     * Default social user mapper. Applications can override by providing
     * their own {@link SocialUserMapper} bean.
     */
    @Bean
    @ConditionalOnMissingBean(SocialUserMapper.class)
    public SocialUserMapper socialUserMapper() {
        return new DefaultSocialUserMapper();
    }

    @Bean
    public CandiOAuth2UserService candiOAuth2UserService(SocialUserMapper userMapper) {
        return new CandiOAuth2UserService(userMapper);
    }
}
