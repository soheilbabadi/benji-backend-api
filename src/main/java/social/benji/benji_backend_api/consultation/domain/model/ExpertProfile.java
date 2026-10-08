package social.benji.benji_backend_api.consultation.domain.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import social.benji.benji_backend_api.consultation.domain.valueobject.ExpertVerificationStatus;
import social.benji.benji_backend_api.lookup.LookupModel;

import java.time.Instant;
import java.util.*;

/**
 * Expert profile aggregate.
 * Represents a verified expert who can answer consultations.
 */
@Document(collection = "expert_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpertProfile {

    @Id
    private String id;
    private String userId; // Links to User collection
    private ExpertVerificationStatus verificationStatus;
    private boolean isActive;
    private String bio;
    private Set<String> specialties;
    private Instant createdAt;
    private Instant updatedAt;
}
