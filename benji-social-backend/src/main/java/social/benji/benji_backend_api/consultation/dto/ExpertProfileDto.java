package social.benji.benji_backend_api.consultation.dto;

import lombok.*;
import social.benji.benji_backend_api.consultation.domain.valueobject.ExpertVerificationStatus;

import java.time.Instant;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpertProfileDto {

    private String id;
    private String userId;
    private ExpertVerificationStatus verificationStatus;
    private boolean active;
    private String bio;
    private Set<String> specialties;
    private Instant createdAt;
    private Instant updatedAt;
}
