package social.benji.benji_backend_api.consultation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String id;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String userId;
    private ExpertVerificationStatus verificationStatus;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean active;
    private String bio;
    private Set<String> specialties;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Instant createdAt;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Instant updatedAt;
}
