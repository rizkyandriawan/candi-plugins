package candi.saas.storage;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * Spring Boot auto-configuration for Candi Storage.
 * Activated when candi.storage.access-key is set.
 *
 * Creates:
 * - S3Client (configured for S3 or Minio)
 * - S3Presigner (for presigned URLs)
 * - StorageService (upload/download/delete operations)
 * - UploadInterceptor (auto-processes @Upload fields on multipart requests)
 * - CndUploadWidget (via component scan)
 */
@AutoConfiguration
@EnableConfigurationProperties(StorageProperties.class)
@ConditionalOnProperty(prefix = "candi.storage", name = "access-key")
@ComponentScan(basePackageClasses = CndUploadWidget.class)
public class CandiStorageAutoConfiguration implements WebMvcConfigurer {

    private final StorageProperties properties;
    private final ApplicationContext applicationContext;

    public CandiStorageAutoConfiguration(StorageProperties properties, ApplicationContext applicationContext) {
        this.properties = properties;
        this.applicationContext = applicationContext;
    }

    @Bean
    public S3Client candiS3Client() {
        return S3ClientFactory.createClient(properties);
    }

    @Bean
    public S3Presigner candiS3Presigner() {
        return S3ClientFactory.createPresigner(properties);
    }

    @Bean
    public StorageService candiStorageService(S3Client candiS3Client, S3Presigner candiS3Presigner) {
        return new StorageService(candiS3Client, candiS3Presigner, properties);
    }

    @Bean
    public UploadInterceptor candiUploadInterceptor(StorageService candiStorageService) {
        return new UploadInterceptor(candiStorageService, properties, applicationContext);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(applicationContext.getBean(UploadInterceptor.class))
                .addPathPatterns("/**");
    }
}
