package social.benji.benji_backend_api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.object-storage")
public class ObjectStorageProperties {
    private long maxFileSizeBytes = 10 * 1024 * 1024;
    private List<String> allowedMimeTypes = List.of("image/jpeg", "image/png", "image/webp");
    private List<String> allowedExtensions = List.of("jpg", "jpeg", "png", "webp");
}
