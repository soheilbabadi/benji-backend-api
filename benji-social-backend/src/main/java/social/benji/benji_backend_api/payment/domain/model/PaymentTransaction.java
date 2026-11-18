package social.benji.benji_backend_api.payment.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import social.benji.benji_backend_api.payment.domain.valueobject.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_transaction")
@Getter
@NoArgsConstructor
public class PaymentTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, columnDefinition = "paid_service_type")
    private PaidServiceType serviceType;

    @Column(name = "service_reference_id", nullable = false)
    private UUID serviceReferenceId;

    @Column(name = "tariff_id", nullable = false)
    private UUID tariffId;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false)
    private String currency = "IRR";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "payment_gateway")
    private PaymentGatewayType gateway = PaymentGatewayType.ZARINPAL;

    @Column(name = "gateway_authority")
    private String gatewayAuthority;

    @Column(name = "gateway_reference_id")
    private String gatewayReferenceId;

    @Column(name = "gateway_request_id")
    private String gatewayRequestId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "payment_status")
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "refunded_at")
    private Instant refundedAt;

    @Column(name = "failure_reason")
    private String failureReason;

    @Version
    private Long version;

    public PaymentTransaction(UUID userId, PaidServiceType serviceType, UUID serviceReferenceId,
                              Tariff tariff) {
        this.userId = userId;
        this.serviceType = serviceType;
        this.serviceReferenceId = serviceReferenceId;
        this.tariffId = tariff.getId();
        this.amount = tariff.getAmount();
        this.currency = tariff.getCurrency();
    }

    public void setGatewayAuthority(String gatewayAuthority) {
        this.gatewayAuthority = gatewayAuthority;
        this.updatedAt = Instant.now();
    }

    public void setGatewayRequestId(String gatewayRequestId) {
        this.gatewayRequestId = gatewayRequestId;
        this.updatedAt = Instant.now();
    }

    public void markSuccess(String gatewayReferenceId) {
        if (status == PaymentStatus.SUCCESS) {
            return;
        }
        if (status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Only PENDING payment can become SUCCESS");
        }
        this.gatewayReferenceId = gatewayReferenceId;
        this.status = PaymentStatus.SUCCESS;
        this.paidAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void markFailed(String reason) {
        if (status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Only PENDING payment can become FAILED");
        }
        this.failureReason = reason;
        this.status = PaymentStatus.FAILED;
        this.updatedAt = Instant.now();
    }

    public void markCancelled(String reason) {
        if (status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Only PENDING payment can become CANCELLED");
        }
        this.failureReason = reason;
        this.status = PaymentStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }
}
