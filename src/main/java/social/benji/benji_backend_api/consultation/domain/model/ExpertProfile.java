package social.benji.benji_backend_api.consultation.domain.model;

import lombok.*;
import social.benji.benji_backend_api.consultation.domain.valueobject.ExpertVerificationStatus;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;
import java.util.UUID;

/**
 * Expert profile aggregate.
 * Represents a verified expert who can answer consultations.
 */
@Entity
@Table(name = "expert_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpertProfile {

    @Id
    private UUID id;
    private UUID userId; // Links to User table
    @Enumerated(EnumType.STRING)
    private ExpertVerificationStatus verificationStatus;
    private boolean isActive;
    private String bio;
    
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> specialties;
    private Instant createdAt;
    private Instant updatedAt;
}
