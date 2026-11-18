package social.benji.benji_backend_api.payment.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.benji.benji_backend_api.payment.application.dto.CreatePaymentRequest;
import social.benji.benji_backend_api.payment.application.dto.PaymentResponse;
import social.benji.benji_backend_api.payment.domain.model.*;
import social.benji.benji_backend_api.payment.domain.repository.*;
import social.benji.benji_backend_api.payment.domain.valueobject.PaidServiceType;
import social.benji.benji_backend_api.payment.domain.valueobject.PaymentStatus;
import social.benji.benji_backend_api.payment.domain.valueobject.PaymentEventType;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentApplicationService implements PaymentUseCase {

    private final TariffRepository tariffRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final PaymentIdempotencyKeyRepository idempotencyRepository;
    private final PaymentEventOutboxRepository outboxRepository;
    private final PaymentGateway paymentGateway;
    private final ObjectMapper objectMapper;

    @Override
    public PaymentResponse createPayment(UUID userId, CreatePaymentRequest request) {
        Optional<PaymentIdempotencyKey> existing = idempotencyRepository.findById(request.idempotencyKey());
        if (existing.isPresent()) {
            PaymentIdempotencyKey key = existing.get();
            if (!key.getUserId().equals(userId)
                    || key.getServiceType() != request.serviceType()
                    || !key.getServiceReferenceId().equals(request.serviceReferenceId())) {
                throw new IllegalArgumentException("Idempotency key does not match payment parameters");
            }
            PaymentTransaction tx = transactionRepository.findById(key.getPaymentTransactionId())
                    .orElseThrow(() -> new IllegalArgumentException("Payment transaction not found"));
            return toResponse(tx);
        }

        PaymentTransaction tx = createTransaction(userId, request);
        PaymentGateway.GatewayRequestResult gatewayResult =
                paymentGateway.requestPayment(tx.getId(), tx.getAmount());

        PaymentTransaction updated = persistGatewayMetadata(tx.getId(), gatewayResult.authority(),
                gatewayResult.paymentUrl());
        return toResponse(updated);
    }

    @Transactional
    public PaymentTransaction createTransaction(UUID userId, CreatePaymentRequest request) {
        Tariff tariff = tariffRepository.findByServiceTypeAndActiveTrue(request.serviceType())
                .orElseThrow(() -> new IllegalArgumentException("No active tariff for service"));

        PaymentTransaction tx = new PaymentTransaction(userId, request.serviceType(),
                request.serviceReferenceId(), tariff);
        PaymentTransaction saved = transactionRepository.saveAndFlush(tx);

        PaymentIdempotencyKey key = new PaymentIdempotencyKey(request.idempotencyKey(),
                userId, request.serviceType(), request.serviceReferenceId(), saved.getId());
        try {
            idempotencyRepository.saveAndFlush(key);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Idempotency key is already in use", e);
        }
        return saved;
    }

    @Transactional
    public PaymentTransaction persistGatewayMetadata(UUID transactionId, String authority,
                                                       String gatewayRequestId) {
        PaymentTransaction tx = transactionRepository.findByIdForUpdate(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Payment transaction not found"));
        tx.setGatewayAuthority(authority);
        tx.setGatewayRequestId(gatewayRequestId);
        return transactionRepository.saveAndFlush(tx);
    }

    @Override
    public PaymentResponse verifyCallback(String authority) {
        Optional<PaymentTransaction> initial =
                transactionRepository.findByGatewayAuthority(authority);
        if (initial.isEmpty()) {
            throw new IllegalArgumentException("Unknown payment authority");
        }
        if (initial.get().getStatus() == PaymentStatus.SUCCESS) {
            return toResponse(initial.get());
        }
        if (initial.get().getStatus() != PaymentStatus.PENDING) {
            return toResponse(initial.get());
        }

        VerificationClaim claim = claimVerification(initial.get().getId());
        if (!claim.successfulClaim()) {
            return toResponse(claim.transaction());
        }

        PaymentGateway.GatewayVerificationResult result;
        try {
            result = paymentGateway.verifyPayment(authority, claim.transaction().getAmount());
        } catch (IllegalStateException e) {
            log.warn("Gateway verification timed out or failed for transaction {}",
                    claim.transaction().getId(), e);
            throw e;
        }

        return toResponse(persistVerification(claim.transaction().getId(), result));
    }

    @Transactional
    public VerificationClaim claimVerification(UUID transactionId) {
        PaymentTransaction tx = transactionRepository.findByIdForUpdate(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Payment transaction not found"));
        if (tx.getStatus() != PaymentStatus.PENDING) {
            return new VerificationClaim(false, tx);
        }
        return new VerificationClaim(true, tx);
    }

    @Transactional
    public PaymentTransaction persistVerification(UUID transactionId,
                                                   PaymentGateway.GatewayVerificationResult result) {
        PaymentTransaction tx = transactionRepository.findByIdForUpdate(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Payment transaction not found"));
        if (tx.getStatus() == PaymentStatus.SUCCESS) {
            return tx;
        }
        if (tx.getStatus() != PaymentStatus.PENDING) {
            return tx;
        }

        if (result.successful()) {
            tx.markSuccess(result.referenceId());
            tx = transactionRepository.saveAndFlush(tx);
            createOutbox(tx, PaymentEventType.PAYMENT_SUCCEEDED);
        } else {
            tx.markFailed(result.failureReason());
            tx = transactionRepository.saveAndFlush(tx);
            createOutbox(tx, PaymentEventType.PAYMENT_FAILED);
        }
        return tx;
    }

    @Transactional
    public void createOutbox(PaymentTransaction tx, PaymentEventType eventType) {
        try {
            Object payload = switch (eventType) {
                case PAYMENT_SUCCEEDED -> new PaymentSucceededPayload(tx);
                case PAYMENT_FAILED -> new PaymentFailedPayload(tx);
                case PAYMENT_REFUNDED -> new PaymentRefundedPayload(tx);
            };
            String json = objectMapper.writeValueAsString(payload);
            outboxRepository.saveAndFlush(new PaymentEventOutbox(eventType, tx.getId(), json));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize payment event", e);
        }
    }

    private PaymentResponse toResponse(PaymentTransaction tx) {
        return new PaymentResponse(tx.getId(), tx.getStatus(), tx.getAmount(),
                tx.getCurrency(), tx.getGatewayAuthority(), tx.getGatewayRequestId());
    }

    public record VerificationClaim(boolean successfulClaim, PaymentTransaction transaction) {}

    public record PaymentSucceededPayload(
            UUID transactionId, UUID userId, PaidServiceType serviceType,
            UUID serviceReferenceId, Long amount, String currency,
            String gatewayReferenceId, Instant occurredAt) {
        public PaymentSucceededPayload(PaymentTransaction tx) {
            this(tx.getId(), tx.getUserId(), tx.getServiceType(),
                    tx.getServiceReferenceId(), tx.getAmount(), tx.getCurrency(),
                    tx.getGatewayReferenceId(), Instant.now());
        }
    }

    public record PaymentFailedPayload(
            UUID transactionId, PaidServiceType serviceType, UUID serviceReferenceId,
            String failureReason, Instant occurredAt) {
        public PaymentFailedPayload(PaymentTransaction tx) {
            this(tx.getId(), tx.getServiceType(), tx.getServiceReferenceId(),
                    tx.getFailureReason(), Instant.now());
        }
    }

    public record PaymentRefundedPayload(
            UUID transactionId, PaidServiceType serviceType, UUID serviceReferenceId,
            Instant occurredAt) {
        public PaymentRefundedPayload(PaymentTransaction tx) {
            this(tx.getId(), tx.getServiceType(), tx.getServiceReferenceId(), Instant.now());
        }
    }
}
