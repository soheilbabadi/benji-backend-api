package social.benji.benji_backend_api.service;

import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import social.benji.benji_backend_api.config.MinioProperties;
import social.benji.benji_backend_api.config.ObjectStorageProperties;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class MinioObjectStorageServiceTest {
    private MinioObjectStorageService service;

    @BeforeEach
    void setUp() {
        MinioProperties minio = new MinioProperties();
        ObjectStorageProperties storage = new ObjectStorageProperties();
        storage.setMaxFileSizeBytes(10 * 1024 * 1024);
        storage.setAllowedMimeTypes(List.of("image/jpeg", "image/png", "image/webp"));
        storage.setAllowedExtensions(List.of("jpg", "jpeg", "png", "webp"));
        service = new MinioObjectStorageService(mock(MinioClient.class), minio, storage);
    }

    @Test
    void generatedObjectKeyNeverUsesOriginalFilenameAndContainsNoCollisions() {
        String first = service.generateObjectKey("profile-pictures", "../../avatar name.PNG");
        String second = service.generateObjectKey("profile-pictures", "../../avatar name.PNG");

        assertNotEquals(first, second);
        assertTrue(first.startsWith("profile-pictures/"));
        assertFalse(first.contains("avatar"));
    }

    @Test
    void rejectsEmptyAndUnsupportedSignatureFiles() {
        MockMultipartFile empty = new MockMultipartFile("file", "empty.png",
                "image/png", new byte[0]);
        assertThrows(IllegalArgumentException.class, () -> service.validate(empty));

        MockMultipartFile text = new MockMultipartFile("file", "fake.png",
                "image/png", "not an image".getBytes());
        assertThrows(IllegalArgumentException.class, () -> service.validate(text));
    }

    @Test
    void acceptsMatchingPngSignatureAndExtension() {
        byte[] png = new byte[]{(byte)0x89, 'P', 'N', 'G',
                13, 10, 26, 10, 0, 0, 0, 0};
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png",
                "image/png", png);
        assertDoesNotThrow(() -> service.validate(file));
    }
}
