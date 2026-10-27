package social.benji.benji_backend_api.usermanagement.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Identity root for Benji. Future business modules reference this row only via
 * {@code user_id} and never duplicate profile data.
 */
@Entity
@Table(name = "tbl_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /** Normalized E.164 international representation, e.g. +989121234567. */
    @Column(name = "mobile_number", nullable = false, unique = true, length = 20)
    private String mobileNumber;

    @Column(name = "mobile_verified", nullable = false)
    @Builder.Default
    private boolean mobileVerified = false;

    @Column(name = "email", length = 254)
    private String email;

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private boolean emailVerified = false;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    /** Unique handle for public profile URLs; lowercase letters, digits, dot and underscore. */
    @Column(name = "username", length = 30, unique = true)
    private String username;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "bio", length = 500)
    private String bio;

    /** Optional secondary contact number shown on the profile (not an identity). */
    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "website", length = 200)
    private String website;

    /** Free-form ISO date (yyyy-MM-dd) to keep the schema tolerant of partial dates. */
    @Column(name = "birth_date", length = 10)
    private String birthDate;

    @Column(name = "gender", length = 20)
    private String gender;

    /**
     * Object-storage key of the avatar inside the shared MinIO bucket, e.g.
     * {@code profile-pictures/<userId>/av_<uuid>.jpg}. Never a client filename
     * and never a full URL; delivery happens through short-lived presigned GETs.
     */
    @Column(name = "avatar_object_key", length = 200)
    private String avatarObjectKey;

    /** MIME type detected from magic bytes at upload time (jpeg/png/webp). */
    @Column(name = "avatar_content_type", length = 50)
    private String avatarContentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    @Builder.Default
    private UserRole role = UserRole.USER;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** Soft deletion marker; the mobile number is released so it can be re-registered. */
    @Column(name = "deleted_at")
    private Instant deletedAt;

    public static User newActive(String normalizedMobile) {
        return User.builder()
                .id(UUID.randomUUID())
                .mobileNumber(normalizedMobile)
                .mobileVerified(true)
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .build();
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE && deletedAt == null;
    }

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
