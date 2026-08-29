package social.benji.benji_backend_api.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfilePictureRequest {

    private MultipartFile file;
}
