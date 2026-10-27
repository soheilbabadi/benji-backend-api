package social.benji.benji_backend_api.payment.domain;

/**
 * The type of paid Benji service a payment transaction belongs to.
 *
 * <p>This is the generic, module-agnostic reference through which other business modules
 * (Lost &amp; Found, Expert Consultation, future services) integrate with Payment.
 * Payment never owns or inspects the lifecycle of the referenced business object.</p>
 */
public enum ServiceType {

    /** Paid advertisement boosting a lost-pet report (Lost &amp; Found module). */
    LOST_PET_ADVERTISEMENT,

    /** Paid expert consultation session (Expert Consultation module). */
    EXPERT_CONSULTATION
}
