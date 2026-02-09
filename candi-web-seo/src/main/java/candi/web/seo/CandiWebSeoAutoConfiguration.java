package candi.web.seo;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Auto-configuration for Candi Web SEO.
 * Registers the SEO interceptor, widgets, sitemap controller, and
 * configuration properties.
 */
@AutoConfiguration
@ComponentScan("candi.web.seo")
@EnableConfigurationProperties(SeoProperties.class)
public class CandiWebSeoAutoConfiguration implements WebMvcConfigurer {

    private final SeoProperties seoProperties;

    public CandiWebSeoAutoConfiguration(SeoProperties seoProperties) {
        this.seoProperties = seoProperties;
    }

    @Bean
    public SeoInterceptor seoInterceptor() {
        return new SeoInterceptor(seoProperties);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(seoInterceptor());
    }
}
