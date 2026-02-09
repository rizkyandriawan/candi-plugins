package candi.data.querybind;

import jakarta.persistence.EntityManager;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring Boot auto-configuration for Candi QueryBind.
 * Activated when an EntityManager is present (JPA is configured).
 *
 * Creates:
 * - QueryBindService (executes JPA Criteria queries from URL params)
 * - QueryBindInterceptor (auto-populates @QueryResult fields)
 */
@AutoConfiguration
@ConditionalOnBean(EntityManager.class)
public class CandiQueryBindAutoConfiguration implements WebMvcConfigurer {

    private final ApplicationContext applicationContext;

    public CandiQueryBindAutoConfiguration(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    public QueryBindService queryBindService(EntityManager entityManager) {
        return new QueryBindService(entityManager);
    }

    @Bean
    public QueryBindInterceptor queryBindInterceptor(QueryBindService queryBindService) {
        return new QueryBindInterceptor(queryBindService, applicationContext);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(applicationContext.getBean(QueryBindInterceptor.class))
                .addPathPatterns("/**");
    }
}
