package social.benji.benji_backend_api.storage;

import java.io.InputStream;
import java.time.Duration;

/**
 * Vendor-neutral object storage port. Business modules depend only on this
 * interface; the MinIO SDK stays isolated in {@link MinioObjectStorage}.
 */
public interface ObjectStorage {

    /**
     * Stores an object under the given key and returns its metadata.
     * Implementations must ensure the target bucket exists before upload.
     */
    StoredObject put(String bucket, String objectKey, InputStream content, long contentLength, String contentType);

    /** Generates a short-lived GET URL so clients fetch bytes directly from storage. */
    String presignedGetUrl(String bucket, String objectKey, Duration expiry);

    void delete(String bucket, String objectKey);

    boolean exists(String bucket, String objectKey);

    record StoredObject(String bucket, String objectKey, long size, String contentType) {
    }
}
