package social.benji.benji_backend_api.payment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Pricing for one paid service type (approved decision A: tariff lives inside the Payment
 * module as a separate entity, not a shared pricing module).
 *
 * <p>Invariants enforced at the database level (Liquibase changesets 007b/011):</p>
 * <ul>
 *   <li>At most ONE active tariff per {@link ServiceType} (partial unique index WHERE is_active).</li>
 *   <li>{@code amount} is NUMERIC(19,0) in integer IRR Rials — approved decision D.</li>
 *   <li>Tariffs are immutable once superseded: an operator deactivates the old row and inserts a
 *       new active row in a single transaction; already-created transactions keep their snapshot.</li>
 * </ul>
 */
@Entity
@Table(name = "tariff")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {})
public class TariffEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 64)
    private ServiceType serviceType;

    /** Integer minor units of IRR (Rials). scale 0; never floating point. */
    @Column(name = "amount", nullable = false, precision = 19, scale = 0)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = CurrencyCodes.IRR;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "activated_at")
    private Instant activatedAt;

    @Column(name = "effective_from")
    private Instant effectiveFrom;

    @Column(name = "effective_to")
    private Instant effectiveTo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
        if (active && activatedAt == null) activatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * Whether this tariff may price NEW payments right now. The DB partial unique index is the
     * authoritative "one active per service" guard; this additionally honours the validity window
     * so a scheduled-but-not-yet-effective or expired tariff is never used even if an operator
     * forgot to flip {@code is_active}.
     */
    public boolean isApplicableAt(Instant instant) {
        if (!active) return false;
        if (effectiveFrom != null && instant.isBefore(effectiveFrom)) return false;
        if (effectiveTo != null && !instant.isBefore(effectiveTo)) return false;
        return true;
    }
}
