package candi.saas.storage;

/**
 * Result of a storage upload operation.
 *
 * @param key         the object key in the bucket
 * @param url         the accessible URL for the file
 * @param bucket      the bucket where the file was stored
 * @param size        the file size in bytes
 * @param contentType the MIME content type of the file
 */
public record StorageResult(
        String key,
        String url,
        String bucket,
        long size,
        String contentType
) {}
