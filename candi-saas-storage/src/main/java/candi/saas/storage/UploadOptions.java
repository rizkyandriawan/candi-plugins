package candi.saas.storage;

import java.util.Set;

/**
 * Options controlling how a file upload is processed and stored.
 *
 * @param bucket       target bucket name
 * @param pathPrefix   path prefix within the bucket
 * @param maxSize      maximum allowed file size in bytes
 * @param allowedTypes set of allowed MIME types (empty means all allowed)
 * @param publicRead   whether to set public-read ACL
 */
public record UploadOptions(
        String bucket,
        String pathPrefix,
        long maxSize,
        Set<String> allowedTypes,
        boolean publicRead
) {

    /**
     * Create UploadOptions from an @Upload annotation and storage properties.
     */
    public static UploadOptions from(Upload annotation, StorageProperties props) {
        String bucket = annotation.bucket().isEmpty()
                ? props.getDefaultBucket()
                : annotation.bucket();

        String pathPrefix = annotation.path();
        if (!pathPrefix.isEmpty() && !pathPrefix.endsWith("/")) {
            pathPrefix = pathPrefix + "/";
        }

        Set<String> allowedTypes = annotation.allowedTypes().length > 0
                ? Set.of(annotation.allowedTypes())
                : Set.of();

        return new UploadOptions(
                bucket,
                pathPrefix,
                annotation.maxSize(),
                allowedTypes,
                annotation.publicRead()
        );
    }

    /**
     * Create default UploadOptions from storage properties.
     */
    public static UploadOptions defaults(StorageProperties props) {
        return new UploadOptions(
                props.getDefaultBucket(),
                "",
                10485760L,
                Set.of(),
                false
        );
    }
}
