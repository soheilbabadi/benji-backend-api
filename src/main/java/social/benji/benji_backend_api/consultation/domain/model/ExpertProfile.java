package social.benji.benji_backend_api.consultation.domain.model;

import lombok.*;
import social.benji.benji_backend_api.consultation.domain.valueobject.ExpertVerificationStatus;
import social.benji.benji_backend_api.lookup.LookupModel;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;
import java.util.UUID;

/**
 * Expert profile aggregate.
 * Represents a verified expert who can answer consultations.
 */
@Entity
@Table(name = "tbl_expert_profiles")
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
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "tbl_expert_profile_specialties",
        joinColumns = @JoinColumn(name = "expert_profile_id"),
        inverseJoinColumns = @JoinColumn(name = "lookup_id")
    )
    private Set<LookupModel> specialties;
    private Instant createdAt;
    private Instant updatedAt;
}
