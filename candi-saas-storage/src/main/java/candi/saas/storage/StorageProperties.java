package candi.saas.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Candi storage integration.
 * Supports both AWS S3 and Minio-compatible object stores.
 *
 * <pre>
 * candi:
 *   storage:
 *     provider: s3          # "s3" or "minio"
 *     region: us-east-1
 *     access-key: AKIA...
 *     secret-key: xxx
 *     default-bucket: uploads
 * </pre>
 */
@ConfigurationProperties(prefix = "candi.storage")
public class StorageProperties {

    /** Storage provider: "s3" or "minio". */
    private String provider = "s3";

    /** Custom endpoint URL. Required for Minio (e.g., "http://localhost:9000"). */
    private String endpoint = "";

    /** AWS region. Default: us-east-1. */
    private String region = "us-east-1";

    /** Access key ID for S3/Minio authentication. */
    private String accessKey = "";

    /** Secret access key for S3/Minio authentication. */
    private String secretKey = "";

    /** Default bucket name for uploads. */
    private String defaultBucket = "uploads";

    /** Optional CDN base URL for public files (e.g., "https://cdn.example.com"). */
    private String cdnUrl = "";

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getDefaultBucket() {
        return defaultBucket;
    }

    public void setDefaultBucket(String defaultBucket) {
        this.defaultBucket = defaultBucket;
    }

    public String getCdnUrl() {
        return cdnUrl;
    }

    public void setCdnUrl(String cdnUrl) {
        this.cdnUrl = cdnUrl;
    }
}
