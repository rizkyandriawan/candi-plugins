package candi.saas.storage;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field on a Candi page to receive upload results.
 * When a multipart request is processed, the UploadInterceptor validates
 * the uploaded file against these constraints and stores it via StorageService.
 * The field is then set to the resulting URL/key.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Upload {

    /**
     * Override the default bucket name.
     * If empty, uses the configured default bucket from StorageProperties.
     */
    String bucket() default "";

    /**
     * Path prefix within the bucket (e.g., "avatars/", "documents/").
     * A trailing slash is added automatically if not present.
     */
    String path() default "";

    /**
     * Maximum file size in bytes. Default: 10MB.
     */
    long maxSize() default 10485760;

    /**
     * Allowed MIME types (e.g., {"image/png", "image/jpeg"}).
     * Empty array means all types are allowed.
     */
    String[] allowedTypes() default {};

    /**
     * Whether to set public-read ACL on the uploaded object.
     */
    boolean publicRead() default false;
}
