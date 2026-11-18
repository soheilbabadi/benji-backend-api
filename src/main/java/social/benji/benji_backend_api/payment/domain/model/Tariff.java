package social.benji.benji_backend_api.payment.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import social.benji.benji_backend_api.payment.domain.valueobject.Money;
import social.benji.benji_backend_api.payment.domain.valueobject.PaidServiceType;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tariff")
@Getter
@NoArgsConstructor
public class Tariff {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, columnDefinition = "paid_service_type")
    private PaidServiceType serviceType;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false)
    private String currency = "IRR";

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(name = "activated_at")
    private Instant activatedAt;

    public Tariff(PaidServiceType serviceType, Money money) {
        this.serviceType = serviceType;
        this.amount = money.toRialMinorUnits();
        this.currency = money.currency();
        this.active = true;
        this.activatedAt = Instant.now();
    }

    public Money money() {
        return Money.fromRials(java.math.BigDecimal.valueOf(amount));
    }
}
