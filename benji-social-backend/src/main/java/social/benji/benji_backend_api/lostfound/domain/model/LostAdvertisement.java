package social.benji.benji_backend_api.lostfound.domain.model;

import social.benji.benji_backend_api.lostfound.domain.valueobject.Breed;
import social.benji.benji_backend_api.lostfound.domain.valueobject.Location;
import social.benji.benji_backend_api.lostfound.domain.valueobject.PetSpecies;
import social.benji.benji_backend_api.lostfound.domain.lifecycle.LostAdvertisementLifecycle;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class LostAdvertisement {
    private final UUID id;
    private final String ownerId;
    private final UUID petId;
    private final PetSpecies species;
    private final Breed breed;
    private final String petName;
    private final String physicalDescription;
    private final String distinguishingCharacteristics;
    private final LocalDate lostDate;
    private final Location location;
    private final String contactInformation;
    private final Instant createdAt;
    private Instant updatedAt;
    private long version;
    private LostAdvertisementLifecycle lifecycle = LostAdvertisementLifecycle.UNDER_REVIEW;
    private Instant approvedAt;
    private Instant paymentDeadlineAt;
    private Instant publishedAt;
    private Instant expiresAt;

    public LostAdvertisement(String ownerId, UUID petId, PetSpecies species, Breed breed,
                             String petName, String physicalDescription,
                             String distinguishingCharacteristics, LocalDate lostDate,
                             Location location, String contactInformation) {
        this.id = UUID.randomUUID();
        this.ownerId = requireOwner(ownerId);
        this.petId = petId;
        this.species = Objects.requireNonNull(species, "Species is required");
        this.breed = breed == null ? Breed.unknown() : breed;
        this.petName = optionalText(petName, "Pet name", 100);
        this.physicalDescription = requiredText(physicalDescription, "Physical description", 2000);
        this.distinguishingCharacteristics = optionalText(
                distinguishingCharacteristics, "Distinguishing characteristics", 1000);
        this.lostDate = Objects.requireNonNull(lostDate, "Lost date is required");
        if (lostDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Lost date cannot be in the future");
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

    public LostAdvertisementLifecycle getLifecycle() { return lifecycle; }
    public Instant getApprovedAt() { return approvedAt; }
    public Instant getPaymentDeadlineAt() { return paymentDeadlineAt; }
    public Instant getPublishedAt() { return publishedAt; }
    public Instant getExpiresAt() { return expiresAt; }

    public void reject() {
        if (lifecycle != LostAdvertisementLifecycle.UNDER_REVIEW) {
            throw new IllegalStateException("Only an advertisement under review can be rejected");
        }
        lifecycle = LostAdvertisementLifecycle.REJECTED;
    }

    public void resubmit() {
        if (lifecycle != LostAdvertisementLifecycle.REJECTED) {
            throw new IllegalStateException("Only a rejected advertisement can be resubmitted");
        }
        lifecycle = LostAdvertisementLifecycle.UNDER_REVIEW;
    }

    public void approve(Instant now) {
        if (lifecycle != LostAdvertisementLifecycle.UNDER_REVIEW) {
            throw new IllegalStateException("Only an advertisement under review can be approved");
        }
        lifecycle = LostAdvertisementLifecycle.PAYMENT_PENDING;
        approvedAt = now;
        paymentDeadlineAt = now.plusSeconds(48L * 60L * 60L);
    }

    public void publishAfterVerifiedPayment(Instant now) {
        if (lifecycle != LostAdvertisementLifecycle.PAYMENT_PENDING) {
            throw new IllegalStateException("Lost advertisement is not awaiting payment");
        }
        lifecycle = LostAdvertisementLifecycle.PUBLISHED;
        publishedAt = now;
        expiresAt = now.plusSeconds(30L * 24L * 60L * 60L);
    }

    public void expire(Instant now) {
        if (lifecycle != LostAdvertisementLifecycle.PUBLISHED
                || expiresAt == null || now.isBefore(expiresAt)) {
            throw new IllegalStateException("Lost advertisement cannot expire");
        }
        lifecycle = LostAdvertisementLifecycle.EXPIRED;
    }

    public void deactivate() {
        if (lifecycle != LostAdvertisementLifecycle.PUBLISHED) {
            throw new IllegalStateException("Only published Lost advertisements can be deactivated");
        }
        lifecycle = LostAdvertisementLifecycle.DEACTIVATED;
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
    public String getPetName() { return petName; }
    public String getPhysicalDescription() { return physicalDescription; }
    public String getDistinguishingCharacteristics() { return distinguishingCharacteristics; }
    public LocalDate getLostDate() { return lostDate; }
    public Location getLocation() { return location; }
    public String getContactInformation() { return contactInformation; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public long getVersion() { return version; }
}
