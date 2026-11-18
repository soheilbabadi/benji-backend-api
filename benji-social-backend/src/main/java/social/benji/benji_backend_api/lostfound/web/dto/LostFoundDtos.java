package social.benji.benji_backend_api.lostfound.web.dto;

import jakarta.validation.constraints.*;
import social.benji.benji_backend_api.lostfound.domain.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** All request/response records for the Lost & Found REST layer (JPA entities are never exposed). */
public final class LostFoundDtos {

    private LostFoundDtos() { }

    // ---- Requests -----------------------------------------------------------------------------

    /** LOST creation. petId mandatory; ownership verified server-side against the principal. */
    public record CreateLostRequest(
            @NotNull UUID petId,
            @NotNull Species species,
            UUID breedId,
            @Size(max = 100) String animalName,
            AnimalSex sex,
            @Size(max = 100) String color,
            @Size(max = 1000) String distinguishingCharacteristics,
            @NotBlank @Size(max = 2000) String description,
            @NotNull @PastOrPresent Instant incidentAt,
            @NotBlank @Size(max = 64) String country,
            @NotBlank @Size(max = 100) String city,
            @Size(max = 150) String area,
            @DecimalMin("-90") @DecimalMax("90") BigDecimal latitude,
            @DecimalMin("-180") @DecimalMax("180") BigDecimal longitude,
            LocationPrecision locationPrecision,
            @NotNull ContactMethod contactMethod,
            @Pattern(regexp = "^\\+?[0-9]{7,20}$", message = "phone must be E.164") String contactPhoneE164,
            @Positive BigDecimal rewardAmountIrr) {
    }

    /** FOUND creation. No petId field exists at all — the API cannot reference a pet profile. */
    public record CreateFoundRequest(
            @NotNull Species species,
            UUID breedId,
            @Size(max = 100) String animalName,
            AnimalSex sex,
            @Size(max = 100) String color,
            @Size(max = 1000) String distinguishingCharacteristics,
            @NotBlank @Size(max = 2000) String description,
            @NotNull @PastOrPresent Instant incidentAt,
            @NotBlank @Size(max = 64) String country,
            @NotBlank @Size(max = 100) String city,
            @Size(max = 150) String area,
            @DecimalMin("-90") @DecimalMax("90") BigDecimal latitude,
            @DecimalMin("-180") @DecimalMax("180") BigDecimal longitude,
            LocationPrecision locationPrecision,
            @NotNull ContactMethod contactMethod,
            @Pattern(regexp = "^\\+?[0-9]{7,20}$", message = "phone must be E.164") String contactPhoneE164) {
    }

    /** Draft update: same shape as create but every field optional except none; status/type immutable. */
    public record UpdateAdvertisementRequest(
            @Size(max = 100) String animalName,
            AnimalSex sex,
            @Size(max = 100) String color,
            @Size(max = 1000) String distinguishingCharacteristics,
            @NotBlank @Size(max = 2000) String description,
            @NotNull @PastOrPresent Instant incidentAt,
            @NotBlank @Size(max = 64) String country,
            @NotBlank @Size(max = 100) String city,
            @Size(max = 150) String area,
            @DecimalMin("-90") @DecimalMax("90") BigDecimal latitude,
            @DecimalMin("-180") @DecimalMax("180") BigDecimal longitude,
            LocationPrecision locationPrecision,
            ContactMethod contactMethod,
            @Pattern(regexp = "^\\+?[0-9]{7,20}$") String contactPhoneE164,
            @Positive BigDecimal rewardAmountIrr) {
    }

    public record ResolveRequest(@Size(max = 500) String note) { }

    public record ReportRequest(@NotNull ReportReason reason, @Size(max = 1000) String details) { }

    public record ReviewReportRequest(@NotNull ReportStatus outcome, @Size(max = 500) String note) { }

    // ---- Responses -------------------------------------------------------------------------------

    /** Public view: phone is ALWAYS masked; no userId/petId/email leak. */
    public record PublicAdvertisementDto(
            UUID publicId,
            AdvertisementType advertisementType,
            AdvertisementStatus status,
            Species species,
            UUID breedId,
            String animalName,
            AnimalSex sex,
            String color,
            String distinguishingCharacteristics,
            String description,
            Instant incidentAt,
            String country,
            String city,
            String area,
            BigDecimal latitude,
            BigDecimal longitude,
            LocationPrecision locationPrecision,
            ContactMethod contactMethod,
            String maskedContactPhone,
            BigDecimal rewardAmountIrr,
            Instant publishedAt,
            Instant expiresAt,
            Instant resolvedAt,
            List<ImageDto> images) {
    }

    /** Owner/admin view: full detail including lifecycle timestamps and unmasked own contact. */
    public record ManagedAdvertisementDto(
            UUID publicId,
            AdvertisementType advertisementType,
            AdvertisementStatus status,
            UUID petId,
            Species species,
            UUID breedId,
            String animalName,
            AnimalSex sex,
            String color,
            String distinguishingCharacteristics,
            String description,
            Instant incidentAt,
            String country,
            String city,
            String area,
            BigDecimal latitude,
            BigDecimal longitude,
            LocationPrecision locationPrecision,
            ContactMethod contactMethod,
            String contactPhoneE164,
            BigDecimal rewardAmountIrr,
            boolean includeInSearch,
            Instant publishedAt,
            Instant expiresAt,
            Instant resolvedAt,
            Instant closedAt,
            Instant createdAt,
            Instant updatedAt,
            long version,
            List<ImageDto> images) {
    }

    public record ImageDto(UUID imageId, String contentType, long sizeBytes, int sortOrder) { }

    /** Body of POST .../contact reveal response. */
    public record ContactRevealResponse(String phone, String note) { }
}
