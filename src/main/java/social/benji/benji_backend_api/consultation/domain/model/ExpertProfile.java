package social.benji.benji_backend_api.consultation.domain.model;

import lombok.Getter;
import social.benji.benji_backend_api.consultation.domain.valueobject.ExpertVerificationStatus;

import java.time.Instant;
import java.util.*;
import java.util.UUID;

/**
 * Expert profile aggregate.
 * Represents a verified expert who can answer consultations.
 */
@Getter
public class ExpertProfile {

    private final UUID id;
    private UUID userId; // Links to User table
    private ExpertVerificationStatus verificationStatus;
    private boolean isActive;
    private String bio;
    private final Set<String> specialties;
    private final Instant createdAt;
    private Instant updatedAt;

    protected ExpertProfile() {
        // For JPA
        this.id = null;
        this.userId = null;
        this.verificationStatus = null;
        this.isActive = false;
        this.specialties = null;
        this.createdAt = null;
        this.updatedAt = null;
    }

    public ExpertProfile(UUID userId) {
        this.id = UUID.randomUUID();
        this.userId = Objects.requireNonNull(userId, "User ID cannot be null");
        this.verificationStatus = ExpertVerificationStatus.PENDING;
        this.isActive = false;
        this.specialties = new HashSet<>();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Adds a specialty to the expert's profile.
     */
    public void addSpecialty(String specialty) {
        Objects.requireNonNull(specialty, "Specialty cannot be null");
        specialties.add(specialty.toUpperCase());
        this.updatedAt = Instant.now();
    }

    /**
     * Removes a specialty from the expert's profile.
     */
    public void removeSpecialty(String specialty) {
        specialties.remove(specialty.toUpperCase());
        this.updatedAt = Instant.now();
    }

    /**
     * Sets verification status. Only VERIFIED experts can answer consultations.
     */
    public void setVerificationStatus(ExpertVerificationStatus status) {
        this.verificationStatus = Objects.requireNonNull(status, "Status cannot be null");
        this.updatedAt = Instant.now();
    }

    /**
     * Activates or deactivates the expert.
     * Only ACTIVE and VERIFIED experts can receive consultations.
     */
    public void setActive(boolean active) {
        this.isActive = active;
        this.updatedAt = Instant.now();
    }

    /**
     * Updates the expert's bio.
     */
    public void updateBio(String bio) {
        this.bio = bio;
        this.updatedAt = Instant.now();
    }

    /**
     * Checks if the expert can accept consultations.
     */
    public boolean canAcceptConsultations() {
        return this.verificationStatus == ExpertVerificationStatus.VERIFIED && this.isActive;
    }

    /**
     * Checks if the expert has a specific specialty.
     */
    public boolean hasSpecialty(String specialty) {
        return specialties.contains(specialty.toUpperCase());
    }
}
