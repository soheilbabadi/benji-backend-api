package social.benji.benji_backend_api.service;

public record UploadedObjectMetadata(
        String objectKey,
        String originalFilename,
        String contentType,
        long size
) {}
