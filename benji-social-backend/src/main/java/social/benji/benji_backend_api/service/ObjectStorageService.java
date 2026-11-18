package social.benji.benji_backend_api.service;

import org.springframework.web.multipart.MultipartFile;

public interface ObjectStorageService {
    UploadedObjectMetadata upload(MultipartFile file, String purpose);
    void delete(String objectKey);
    boolean exists(String objectKey);
    UploadedObjectMetadata metadata(String objectKey);
    String presignedDownloadUrl(String objectKey);
}
