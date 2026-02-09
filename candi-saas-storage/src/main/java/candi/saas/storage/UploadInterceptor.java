package candi.saas.storage;

import candi.runtime.CandiHandlerMapping;
import candi.runtime.CandiPage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * HandlerInterceptor that processes file uploads for Candi pages with @Upload fields.
 *
 * Before the page's onPost() runs, this interceptor:
 * 1. Checks if the request is multipart and the page has @Upload-annotated fields
 * 2. Validates each uploaded file against the @Upload constraints
 * 3. Uploads files to S3/Minio via StorageService
 * 4. Sets the field value on the page bean to the StorageResult or URL string
 */
public class UploadInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(UploadInterceptor.class);

    private final StorageService storageService;
    private final StorageProperties properties;
    private final ApplicationContext applicationContext;

    public UploadInterceptor(StorageService storageService, StorageProperties properties,
                             ApplicationContext applicationContext) {
        this.storageService = storageService;
        this.properties = properties;
        this.applicationContext = applicationContext;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {

        if (!(handler instanceof CandiHandlerMapping.CandiPageHandler pageHandler)) {
            return true;
        }

        if (!(request instanceof MultipartHttpServletRequest multipartRequest)) {
            return true;
        }

        String beanName = pageHandler.beanName();
        CandiPage page = applicationContext.getBean(beanName, CandiPage.class);

        List<UploadFieldMapping> uploadFields = findUploadFields(page.getClass());
        if (uploadFields.isEmpty()) {
            return true;
        }

        for (UploadFieldMapping mapping : uploadFields) {
            String paramName = mapping.field.getName();
            MultipartFile file = multipartRequest.getFile(paramName);

            if (file == null || file.isEmpty()) {
                continue;
            }

            UploadOptions options = UploadOptions.from(mapping.annotation, properties);

            try {
                StorageResult result = storageService.upload(file, options);
                setFieldValue(page, mapping.field, result);
                log.debug("Uploaded file for field '{}' -> {}", paramName, result.url());
            } catch (StorageException e) {
                log.error("Upload failed for field '{}': {}", paramName, e.getMessage());
                throw e;
            }
        }

        return true;
    }

    private List<UploadFieldMapping> findUploadFields(Class<?> pageClass) {
        List<UploadFieldMapping> mappings = new ArrayList<>();
        Class<?> current = pageClass;

        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                Upload annotation = field.getAnnotation(Upload.class);
                if (annotation != null) {
                    mappings.add(new UploadFieldMapping(field, annotation));
                }
            }
            current = current.getSuperclass();
        }

        return mappings;
    }

    private void setFieldValue(CandiPage page, Field field, StorageResult result) {
        field.setAccessible(true);
        try {
            Class<?> fieldType = field.getType();
            if (fieldType == StorageResult.class) {
                field.set(page, result);
            } else if (fieldType == String.class) {
                field.set(page, result.url());
            } else {
                log.warn("@Upload field '{}' has unsupported type '{}'. Expected String or StorageResult.",
                        field.getName(), fieldType.getName());
            }
        } catch (IllegalAccessException e) {
            throw new StorageException("Failed to set upload result on field: " + field.getName(), e);
        }
    }

    private record UploadFieldMapping(Field field, Upload annotation) {}
}
