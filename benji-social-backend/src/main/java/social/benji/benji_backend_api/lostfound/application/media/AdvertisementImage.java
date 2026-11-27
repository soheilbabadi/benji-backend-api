package social.benji.benji_backend_api.lostfound.application.media;

import java.util.UUID;

public record AdvertisementImage(
        UUID advertisementId,
        String objectKey,
        String originalFilename,
        String contentType,
        long sizeBytes
) {
    public AdvertisementImage {
        if (advertisementId == null) {
            throw new IllegalArgumentException("Advertisement ID is required");
        }
        if (objectKey == null || objectKey.isBlank()) {
            throw new IllegalArgumentException("Object key is required");
        }
        if (sizeBytes < 0) {
            throw new IllegalArgumentException("Image size cannot be negative");
        }
    }
}
