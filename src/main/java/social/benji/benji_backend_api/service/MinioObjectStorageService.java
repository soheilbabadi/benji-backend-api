package social.benji.benji_backend_api.service;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.http.Method;
import io.minio.PutObjectArgs;
import io.minio.StatObjectResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import social.benji.benji_backend_api.config.MinioProperties;
import social.benji.benji_backend_api.config.ObjectStorageProperties;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@EnableConfigurationProperties({MinioProperties.class, ObjectStorageProperties.class})
public class MinioObjectStorageService implements ObjectStorageService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final ObjectStorageProperties objectStorageProperties;

    @Override
    public UploadedObjectMetadata upload(MultipartFile file, String purpose) {
        validate(file);
        ensureBucket();
        String key = generateObjectKey(purpose, file.getOriginalFilename());
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(key)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(detectMimeType(file))
                    .build());
            return new UploadedObjectMetadata(key, file.getOriginalFilename(),
                    detectMimeType(file), file.getSize());
        } catch (Exception e) {
            throw new ObjectStorageException("Failed to upload object", e);
        }
    }

    @Override
    public void delete(String objectKey) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectKey)
                    .build());
        } catch (Exception e) {
            throw new ObjectStorageException("Failed to delete object: " + objectKey, e);
        }
    }

    @Override
    public boolean exists(String objectKey) {
        try {
            metadata(objectKey);
            return true;
        } catch (ObjectStorageException e) {
            return false;
        }
    }

    @Override
    public UploadedObjectMetadata metadata(String objectKey) {
        try {
            StatObjectResponse response = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectKey)
                    .build());
            return new UploadedObjectMetadata(objectKey, null,
                    response.contentType(), response.size());
        } catch (Exception e) {
            throw new ObjectStorageException("Object does not exist or is inaccessible: " + objectKey, e);
        }
    }

    @Override
    public String presignedDownloadUrl(String objectKey) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectKey)
                    .method(Method.GET)
                    .expiry((int) minioProperties.getPresignedUrlExpirationSeconds())
                    .build());
        } catch (Exception e) {
            throw new ObjectStorageException("Failed to generate object download URL", e);
        }
    }

    String generateObjectKey(String purpose, String originalFilename) {
        String safePurpose = (purpose == null ? "objects" : purpose)
                .replaceAll("[^A-Za-z0-9_-]", "_");
        String extension = extension(originalFilename);
        LocalDate now = LocalDate.now();
        return safePurpose + "/" + now.getYear() + "/" + String.format("%02d", now.getMonthValue())
                + "/" + UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);
    }

    void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }
        if (file.getSize() > objectStorageProperties.getMaxFileSizeBytes()) {
            throw new IllegalArgumentException("Uploaded file exceeds the configured size limit");
        }

        String extension = extension(file.getOriginalFilename()).toLowerCase(Locale.ROOT);
        if (!objectStorageProperties.getAllowedExtensions().contains(extension)) {
            throw new IllegalArgumentException("Unsupported file extension");
        }

        String detectedType = detectMimeType(file);
        if (!objectStorageProperties.getAllowedMimeTypes().contains(detectedType)) {
            throw new IllegalArgumentException("Unsupported file content type");
        }
    }

    private String detectMimeType(MultipartFile file) {
        try (InputStream input = file.getInputStream()) {
            byte[] header = input.readNBytes(12);
            if (header.length >= 8 && header[0] == (byte)0x89 && header[1] == 'P'
                    && header[2] == 'N' && header[3] == 'G') {
                return "image/png";
            }
            if (header.length >= 3 && header[0] == (byte)0xff
                    && header[1] == (byte)0xd8 && header[2] == (byte)0xff) {
                return "image/jpeg";
            }
            if (header.length >= 12 && new String(header, 0, 4).equals("RIFF")
                    && new String(header, 8, 4).equals("WEBP")) {
                return "image/webp";
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to inspect uploaded file", e);
        }
        throw new IllegalArgumentException("Uploaded file signature is not a supported image");
    }

    private String extension(String filename) {
        if (filename == null) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1).toLowerCase(Locale.ROOT) : "";
    }

    private void ensureBucket() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(minioProperties.getBucketName())
                        .build());
            }
        } catch (Exception e) {
            throw new ObjectStorageException("Failed to initialize private object bucket", e);
        }
    }
}
