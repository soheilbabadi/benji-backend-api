package social.benji.benji_backend_api.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String userId;
    private String email;
    private String mobileNumber;
    private String firstName;
    private String lastName;
    private String profilePictureFileId;
    private boolean authenticated;
    private String message;
}
