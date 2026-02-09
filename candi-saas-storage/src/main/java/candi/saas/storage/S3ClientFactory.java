package candi.saas.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

/**
 * Factory for creating S3Client and S3Presigner instances based on StorageProperties.
 * Handles both AWS S3 and Minio configurations, and auto-creates the default bucket
 * if it does not exist.
 */
public class S3ClientFactory {

    private static final Logger log = LoggerFactory.getLogger(S3ClientFactory.class);

    private S3ClientFactory() {}

    /**
     * Create an S3Client configured for the given storage properties.
     */
    public static S3Client createClient(StorageProperties props) {
        var builder = S3Client.builder()
                .region(Region.of(props.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(props.getAccessKey(), props.getSecretKey())
                ));

        if (isMinio(props)) {
            builder.endpointOverride(URI.create(props.getEndpoint()))
                    .serviceConfiguration(S3Configuration.builder()
                            .pathStyleAccessEnabled(true)
                            .build())
                    .forcePathStyle(true);
        } else if (!props.getEndpoint().isEmpty()) {
            builder.endpointOverride(URI.create(props.getEndpoint()));
        }

        S3Client client = builder.build();
        ensureBucketExists(client, props.getDefaultBucket());
        return client;
    }

    /**
     * Create an S3Presigner configured for the given storage properties.
     */
    public static S3Presigner createPresigner(StorageProperties props) {
        var builder = S3Presigner.builder()
                .region(Region.of(props.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(props.getAccessKey(), props.getSecretKey())
                ));

        if (isMinio(props)) {
            builder.endpointOverride(URI.create(props.getEndpoint()))
                    .serviceConfiguration(S3Configuration.builder()
                            .pathStyleAccessEnabled(true)
                            .build());
        } else if (!props.getEndpoint().isEmpty()) {
            builder.endpointOverride(URI.create(props.getEndpoint()));
        }

        return builder.build();
    }

    private static boolean isMinio(StorageProperties props) {
        return "minio".equalsIgnoreCase(props.getProvider());
    }

    private static void ensureBucketExists(S3Client client, String bucketName) {
        try {
            client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
            log.debug("Bucket '{}' already exists", bucketName);
        } catch (NoSuchBucketException e) {
            log.info("Bucket '{}' does not exist, creating it", bucketName);
            client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
        } catch (Exception e) {
            log.warn("Could not verify bucket '{}': {}", bucketName, e.getMessage());
        }
    }
}
