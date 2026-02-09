package candi.auth.social;

import candi.auth.core.AuthRequestHolder;
import candi.auth.core.CandiUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Spring Security configuration for OAuth2 social login.
 *
 * <p>Automatically registers {@link ClientRegistration} instances for each
 * provider configured in {@link SocialAuthProperties}. Configures success
 * and failure handlers that integrate with Candi's auth model.</p>
 */
@Configuration
@EnableWebSecurity
public class SocialAuthConfigurer {

    private static final Logger log = LoggerFactory.getLogger(SocialAuthConfigurer.class);

    private final SocialAuthProperties properties;
    private final CandiOAuth2UserService oAuth2UserService;

    public SocialAuthConfigurer(SocialAuthProperties properties,
                                CandiOAuth2UserService oAuth2UserService) {
        this.properties = properties;
        this.oAuth2UserService = oAuth2UserService;
    }

    @Bean
    public SecurityFilterChain candiSocialSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**")
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oAuth2UserService)
                        )
                        .successHandler(socialAuthSuccessHandler())
                        .failureHandler(new SimpleUrlAuthenticationFailureHandler(
                                properties.getFailureUrl()))
                );

        return http.build();
    }

    @Bean
    public ClientRegistrationRepository candiClientRegistrationRepository() {
        List<ClientRegistration> registrations = new ArrayList<>();

        for (Map.Entry<String, SocialAuthProperties.ProviderConfig> entry :
                properties.getProviders().entrySet()) {
            String provider = entry.getKey();
            SocialAuthProperties.ProviderConfig config = entry.getValue();

            if (config.getClientId() == null || config.getClientId().isEmpty()) {
                log.warn("Skipping OAuth2 provider '{}': no client-id configured", provider);
                continue;
            }

            try {
                ClientRegistration registration = SocialProviderDefaults
                        .forProvider(provider, config.getClientId(), config.getClientSecret(),
                                config.getScopes())
                        .build();
                registrations.add(registration);
                log.info("Registered OAuth2 provider: {}", provider);
            } catch (IllegalArgumentException e) {
                log.warn("Could not register OAuth2 provider '{}': {}", provider, e.getMessage());
            }
        }

        if (registrations.isEmpty()) {
            throw new IllegalStateException(
                    "No OAuth2 providers configured. Add at least one provider under candi.auth.social.providers");
        }

        return new InMemoryClientRegistrationRepository(registrations);
    }

    private AuthenticationSuccessHandler socialAuthSuccessHandler() {
        return (request, response, authentication) -> {
            // Extract CandiUser from OAuth2 principal and store in request
            if (authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
                CandiUser candiUser = CandiOAuth2UserService.extractCandiUser(oAuth2User);
                if (candiUser != null) {
                    AuthRequestHolder.setCurrentUser(request, candiUser);
                }
            }

            response.sendRedirect(properties.getSuccessUrl());
        };
    }
}
