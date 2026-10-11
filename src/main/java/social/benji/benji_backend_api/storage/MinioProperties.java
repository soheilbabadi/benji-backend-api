package social.benji.benji_backend_api.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Externalized MinIO connection settings. Credentials must come from the
 * environment; never commit real values to source control.
 */
@ConfigurationProperties(prefix = "app.minio")
public record MinioProperties(
        String endpoint,
        String accessKey,
        String secretKey,
        String bucketName,
        int presignExpiryMinutes) {

    public MinioProperties {
        if (presignExpiryMinutes <= 0) {
            presignExpiryMinutes = 5;
        }
    }
}
