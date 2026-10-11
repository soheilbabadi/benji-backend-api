package social.benji.benji_backend_api.storage;

import java.io.InputStream;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.Http;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import lombok.extern.slf4j.Slf4j;

/**
 * MinIO adapter for the {@link ObjectStorage} port. This is the only place in
 * the codebase allowed to import the MinIO SDK.
 */
@Slf4j
@Configuration
class MinioStorageConfig {

    @Bean
    ObjectStorage objectStorage(MinioProperties properties) {
        MinioClient client = MinioClient.builder()
                .endpoint(properties.endpoint())
                .credentials(properties.accessKey(), properties.secretKey())
                .build();
        return new MinioObjectStorage(client);
    }
}

@Slf4j
class MinioObjectStorage implements ObjectStorage {

    private final MinioClient client;
    /** Avoid a BucketExists round-trip on every upload. */
    private final ConcurrentHashMap<String, Boolean> verifiedBuckets = new ConcurrentHashMap<>();

    MinioObjectStorage(MinioClient client) {
        this.client = client;
    }

    @Override
    public StoredObject put(String bucket, String objectKey, InputStream content,
                            long contentLength, String contentType) {
        ensureBucket(bucket);
        try {
            client.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .stream(content, contentLength, -1L)
                    .contentType(contentType)
                    .build());
            log.debug("Stored object {} in bucket {}", objectKey, bucket);
            return new StoredObject(bucket, objectKey, contentLength, contentType);
        } catch (Exception e) {
            throw new StorageException("Failed to store object", e);
        }
    }

    @Override
    public String presignedGetUrl(String bucket, String objectKey, Duration expiry) {
        try {
            return client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Http.Method.GET)
                    .bucket(bucket)
                    .object(objectKey)
                    .expiry((int) expiry.toSeconds())
                    .build());
        } catch (Exception e) {
            throw new StorageException("Failed to generate access URL", e);
        }
    }

    @Override
    public void delete(String bucket, String objectKey) {
        try {
            client.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .build());
            log.debug("Deleted object {} from bucket {}", objectKey, bucket);
        } catch (Exception e) {
            throw new StorageException("Failed to delete object", e);
        }
    }

    @Override
    public boolean exists(String bucket, String objectKey) {
        try {
            client.statObject(StatObjectArgs.builder().bucket(bucket).object(objectKey).build());
            return true;
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                return false;
            }
            throw new StorageException("Failed to stat object", e);
        } catch (Exception e) {
            throw new StorageException("Failed to stat object", e);
        }
    }

    InputStream download(String bucket, String objectKey) {
        try {
            return client.getObject(GetObjectArgs.builder().bucket(bucket).object(objectKey).build());
        } catch (Exception e) {
            throw new StorageException("Failed to download object", e);
        }
    }

    private void ensureBucket(String bucket) {
        verifiedBuckets.computeIfAbsent(bucket, b -> {
            try {
                boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(b).build());
                if (!exists) {
                    client.makeBucket(MakeBucketArgs.builder().bucket(b).build());
                }
                return Boolean.TRUE;
            } catch (Exception e) {
                throw new StorageException("Failed to ensure bucket " + b, e);
            }
        });
    }

    static class StorageException extends RuntimeException {
        StorageException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
