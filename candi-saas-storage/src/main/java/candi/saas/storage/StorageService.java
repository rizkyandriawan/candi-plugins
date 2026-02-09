package candi.saas.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;

/**
 * Service for uploading, downloading, and managing files in S3-compatible storage.
 * Works with both AWS S3 and Minio.
 */
public class StorageService {

    private static final Logger log = LoggerFactory.getLogger(StorageService.class);

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final StorageProperties properties;

    public StorageService(S3Client s3Client, S3Presigner s3Presigner, StorageProperties properties) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.properties = properties;
    }

    /**
     * Upload a MultipartFile using the given options.
     *
     * @param file    the uploaded file
     * @param options upload configuration
     * @return result containing the key, URL, bucket, size, and content type
     */
    public StorageResult upload(MultipartFile file, UploadOptions options) {
        validateFile(file, options);

        String originalFilename = file.getOriginalFilename();
        String contentType = file.getContentType();
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        try {
            return upload(file.getInputStream(), originalFilename, contentType, file.getSize(), options);
        } catch (IOException e) {
            throw new StorageException("Failed to read uploaded file: " + originalFilename, e);
        }
    }

    /**
     * Upload from an InputStream using the given options.
     *
     * @param input       the input stream
     * @param filename    the original filename
     * @param contentType the MIME content type
     * @param options     upload configuration
     * @return result containing the key, URL, bucket, size, and content type
     */
    public StorageResult upload(InputStream input, String filename, String contentType, UploadOptions options) {
        return upload(input, filename, contentType, -1, options);
    }

    private StorageResult upload(InputStream input, String filename, String contentType, long knownSize,
                                 UploadOptions options) {
        String key = generateKey(filename, options.pathPrefix());
        String bucket = options.bucket();

        try {
            byte[] bytes = input.readAllBytes();
            long size = bytes.length;

            var putBuilder = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType);

            if (options.publicRead()) {
                putBuilder.acl(ObjectCannedACL.PUBLIC_READ);
            }

            s3Client.putObject(putBuilder.build(), RequestBody.fromBytes(bytes));

            String url = resolveUrl(bucket, key);
            log.info("Uploaded '{}' to {}/{} ({} bytes)", filename, bucket, key, size);

            return new StorageResult(key, url, bucket, size, contentType);
        } catch (IOException e) {
            throw new StorageException("Failed to read input stream for: " + filename, e);
        } catch (S3Exception e) {
            throw new StorageException("S3 upload failed for key: " + key, e);
        }
    }

    /**
     * Download a file by its key from the default bucket.
     *
     * @param key the object key
     * @return an InputStream for reading the file contents
     */
    public InputStream download(String key) {
        return download(key, properties.getDefaultBucket());
    }

    /**
     * Download a file by its key from a specific bucket.
     *
     * @param key    the object key
     * @param bucket the bucket name
     * @return an InputStream for reading the file contents
     */
    public InputStream download(String key, String bucket) {
        try {
            return s3Client.getObject(GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
        } catch (NoSuchKeyException e) {
            throw new StorageException("File not found: " + key, e);
        } catch (S3Exception e) {
            throw new StorageException("Failed to download: " + key, e);
        }
    }

    /**
     * Delete a file by its key from the default bucket.
     *
     * @param key the object key
     */
    public void delete(String key) {
        delete(key, properties.getDefaultBucket());
    }

    /**
     * Delete a file by its key from a specific bucket.
     *
     * @param key    the object key
     * @param bucket the bucket name
     */
    public void delete(String key, String bucket) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
            log.info("Deleted {}/{}", bucket, key);
        } catch (S3Exception e) {
            throw new StorageException("Failed to delete: " + key, e);
        }
    }

    /**
     * Get the URL for a file. Uses CDN URL if configured, otherwise builds the S3 URL.
     *
     * @param key the object key
     * @return the file URL
     */
    public String getUrl(String key) {
        return resolveUrl(properties.getDefaultBucket(), key);
    }

    /**
     * Generate a presigned URL for temporary access to a private file.
     *
     * @param key    the object key
     * @param expiry the duration before the URL expires
     * @return a presigned URL string
     */
    public String getPresignedUrl(String key, Duration expiry) {
        return getPresignedUrl(key, properties.getDefaultBucket(), expiry);
    }

    /**
     * Generate a presigned URL for temporary access to a private file in a specific bucket.
     *
     * @param key    the object key
     * @param bucket the bucket name
     * @param expiry the duration before the URL expires
     * @return a presigned URL string
     */
    public String getPresignedUrl(String key, String bucket, Duration expiry) {
        try {
            var presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(expiry)
                    .getObjectRequest(GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build())
                    .build();

            return s3Presigner.presignGetObject(presignRequest).url().toString();
        } catch (Exception e) {
            throw new StorageException("Failed to generate presigned URL for: " + key, e);
        }
    }

    /**
     * Check if a file exists in the default bucket.
     *
     * @param key the object key
     * @return true if the file exists
     */
    public boolean exists(String key) {
        return exists(key, properties.getDefaultBucket());
    }

    /**
     * Check if a file exists in a specific bucket.
     *
     * @param key    the object key
     * @param bucket the bucket name
     * @return true if the file exists
     */
    public boolean exists(String key, String bucket) {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            throw new StorageException("Failed to check existence: " + key, e);
        }
    }

    private void validateFile(MultipartFile file, UploadOptions options) {
        if (file == null || file.isEmpty()) {
            throw new StorageException("Upload file is empty or null");
        }

        if (file.getSize() > options.maxSize()) {
            throw new StorageException(String.format(
                    "File size %d bytes exceeds maximum allowed %d bytes",
                    file.getSize(), options.maxSize()));
        }

        if (!options.allowedTypes().isEmpty() && file.getContentType() != null
                && !options.allowedTypes().contains(file.getContentType())) {
            throw new StorageException(String.format(
                    "Content type '%s' is not allowed. Allowed types: %s",
                    file.getContentType(), options.allowedTypes()));
        }
    }

    private String generateKey(String filename, String pathPrefix) {
        String extension = "";
        if (filename != null && filename.contains(".")) {
            extension = filename.substring(filename.lastIndexOf('.'));
        }
        String uniqueName = UUID.randomUUID().toString() + extension;
        return pathPrefix.isEmpty() ? uniqueName : pathPrefix + uniqueName;
    }

    private String resolveUrl(String bucket, String key) {
        if (!properties.getCdnUrl().isEmpty()) {
            String cdnBase = properties.getCdnUrl();
            if (cdnBase.endsWith("/")) {
                cdnBase = cdnBase.substring(0, cdnBase.length() - 1);
            }
            return cdnBase + "/" + key;
        }

        if ("minio".equalsIgnoreCase(properties.getProvider()) && !properties.getEndpoint().isEmpty()) {
            String endpoint = properties.getEndpoint();
            if (endpoint.endsWith("/")) {
                endpoint = endpoint.substring(0, endpoint.length() - 1);
            }
            return endpoint + "/" + bucket + "/" + key;
        }

        // Standard S3 URL
        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                bucket, properties.getRegion(), key);
    }
}
