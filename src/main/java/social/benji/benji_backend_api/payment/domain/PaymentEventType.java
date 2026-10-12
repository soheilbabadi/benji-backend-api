package social.benji.benji_backend_api.payment.domain;

/** Outbox event types (mirrors the payment_event_type DB enum from changeset 004). */
public enum PaymentEventType {
    PAYMENT_SUCCEEDED,
    PAYMENT_FAILED,
    PAYMENT_REFUNDED
}
