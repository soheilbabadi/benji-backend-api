package social.benji.benji_backend_api.lostfound.domain.model;

import social.benji.benji_backend_api.lostfound.domain.valueobject.Breed;
import social.benji.benji_backend_api.lostfound.domain.valueobject.Location;
import social.benji.benji_backend_api.lostfound.domain.valueobject.PetSpecies;
import social.benji.benji_backend_api.lostfound.domain.lifecycle.FoundAdvertisementLifecycle;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class FoundAdvertisement {
    private final UUID id;
    private final String ownerId;
    private final UUID petId;
    private final PetSpecies species;
    private final Breed breed;
    private final String physicalDescription;
    private final String distinguishingCharacteristics;
    private final LocalDate foundDate;
    private final Location location;
    private final String contactInformation;
    private final Instant createdAt;
    private Instant updatedAt;
    private long version;
    private FoundAdvertisementLifecycle lifecycle = FoundAdvertisementLifecycle.UNDER_REVIEW;
    private Instant approvedAt;
    private Instant publishedAt;
    private Instant expiresAt;

    public FoundAdvertisement(String ownerId, UUID petId, PetSpecies species, Breed breed,
                              String physicalDescription, String distinguishingCharacteristics,
                              LocalDate foundDate, Location location, String contactInformation) {
        this.id = UUID.randomUUID();
        this.ownerId = requireOwner(ownerId);
        this.petId = petId;
        this.species = Objects.requireNonNull(species, "Species is required");
        this.breed = breed == null ? Breed.unknown() : breed;
        this.physicalDescription = requiredText(
                physicalDescription, "Physical description", 2000);
        this.distinguishingCharacteristics = optionalText(
                distinguishingCharacteristics, "Distinguishing characteristics", 1000);
        this.foundDate = Objects.requireNonNull(foundDate, "Found date is required");
        if (foundDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Found date cannot be in the future");
        }
        this.location = Objects.requireNonNull(location, "Location is required");
        this.contactInformation = optionalText(contactInformation, "Contact information", 500);
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.version = 0;
    }

    public boolean isOwnedBy(String requestedOwnerId) {
        return ownerId.equals(requestedOwnerId);
    }

    public FoundAdvertisementLifecycle getLifecycle() { return lifecycle; }
    public Instant getApprovedAt() { return approvedAt; }
    public Instant getPublishedAt() { return publishedAt; }
    public Instant getExpiresAt() { return expiresAt; }

    public void reject() {
        if (lifecycle != FoundAdvertisementLifecycle.UNDER_REVIEW) {
            throw new IllegalStateException("Only an advertisement under review can be rejected");
        }
        lifecycle = FoundAdvertisementLifecycle.REJECTED;
    }

    public void resubmit() {
        if (lifecycle != FoundAdvertisementLifecycle.REJECTED) {
            throw new IllegalStateException("Only a rejected advertisement can be resubmitted");
        }
        lifecycle = FoundAdvertisementLifecycle.UNDER_REVIEW;
    }

    public void approve(Instant now) {
        if (lifecycle != FoundAdvertisementLifecycle.UNDER_REVIEW) {
            throw new IllegalStateException("Only an advertisement under review can be approved");
        }
        lifecycle = FoundAdvertisementLifecycle.PUBLISHED;
        approvedAt = now;
        publishedAt = now;
        expiresAt = now.plusSeconds(30L * 24L * 60L * 60L);
    }

    public void expire(Instant now) {
        if (lifecycle != FoundAdvertisementLifecycle.PUBLISHED
                || expiresAt == null || now.isBefore(expiresAt)) {
            throw new IllegalStateException("Found advertisement cannot expire");
        }
        lifecycle = FoundAdvertisementLifecycle.EXPIRED;
    }

    public void deactivate() {
        if (lifecycle != FoundAdvertisementLifecycle.PUBLISHED) {
            throw new IllegalStateException("Only published Found advertisements can be deactivated");
        }
        lifecycle = FoundAdvertisementLifecycle.DEACTIVATED;
    }

    private static String requireOwner(String ownerId) {
        if (ownerId == null || ownerId.isBlank()) {
            throw new IllegalArgumentException("Owner identity is required");
        }
        return ownerId;
    }

    private static String requiredText(String value, String name, int maximum) {
        String normalized = optionalText(value, name, maximum);
        if (normalized == null) {
            throw new IllegalArgumentException(name + " is required");
        }
        return normalized;
    }

    private static String optionalText(String value, String name, int maximum) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.length() > maximum) {
            throw new IllegalArgumentException(name + " must not exceed " + maximum + " characters");
        }
        return normalized;
    }

    public UUID getId() { return id; }
    public String getOwnerId() { return ownerId; }
    public UUID getPetId() { return petId; }
    public PetSpecies getSpecies() { return species; }
    public Breed getBreed() { return breed; }
    public String getPhysicalDescription() { return physicalDescription; }
    public String getDistinguishingCharacteristics() { return distinguishingCharacteristics; }
    public LocalDate getFoundDate() { return foundDate; }
    public Location getLocation() { return location; }
    public String getContactInformation() { return contactInformation; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public long getVersion() { return version; }
}
