package candi.auth.classic;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration for classic session-based authentication.
 *
 * <p>Key design decisions:</p>
 * <ul>
 *   <li>Form login is disabled — Candi handles its own login forms via {@code @Page}</li>
 *   <li>CSRF protection is enabled (session-based apps need it)</li>
 *   <li>All paths are permitted by default — authorization is handled by
 *       Candi's {@code @Protected} annotation via the interceptor</li>
 *   <li>Session management uses the default Spring strategy</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class ClassicSecurityConfig {

    @Bean
    public SecurityFilterChain candiClassicSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**")
                );

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider candiAuthenticationProvider(
            UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager candiAuthenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder candiPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
