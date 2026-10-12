package social.benji.benji_backend_api.payment.domain;

/**
 * Lifecycle status of a payment transaction (the money movement), NOT the lifecycle of the
 * associated business operation (e.g. an advertisement stays DRAFT until the owning module
 * reacts to a verified {@code PaymentSucceededEvent}).
 *
 * <h2>State machine</h2>
 * <pre>
 *   (initiate) --&gt; PENDING --&gt; VERIFYING --&gt; SUCCESS --&gt; REFUNDED
 *                    |  ^          |             (terminal except refund)
 *                    |  |          v
 *                    |  +------- UNKNOWN (outcome uncertain, retry)
 *                    v                |
 *                 CANCELLED          v
 *                 FAILED  &lt;-----------+ (definitive gateway failure only)
 * </pre>
 *
 * <h2>Invariants (approved design decisions B/C/D/G)</h2>
 * <ul>
 *   <li>{@link #FAILED}, {@link #CANCELLED} and {@link #REFUNDED} are terminal;
 *       {@link #SUCCESS} is terminal for everything except a recorded refund.</li>
 *   <li>A verified SUCCESS can NEVER be moved to FAILED/CANCELLED by a duplicate or delayed
 *       callback — see {@link #canTransitionTo}.</li>
 *   <li>{@link #UNKNOWN} means "gateway outcome uncertain" (timeout, dropped response). It is
 *       never terminal and must be retried by callback replay or the reconciliation job.
 *       A timeout alone never justifies FAILED.</li>
 *   <li>Payment attempts are separate transactions (no payment_order entity); at most one
 *       in-flight attempt (PENDING/VERIFYING/UNKNOWN) and at most one SUCCESS per business
 *       operation, enforced by partial unique indexes in Liquibase.</li>
 * </ul>
 */
public enum PaymentStatus {

    /** Created locally; gateway request may or may not have been issued yet. */
    PENDING,

    /** Server-side verification call in progress (transient, guarded by row lock). */
    VERIFYING,

    /** Verification attempted but the gateway outcome is unknown; retry required. */
    UNKNOWN,

    /** Verified server-side by ZarinPal verify (result code 100, or 101/102 already-verified). */
    SUCCESS,

    /** Definitive failure reported by the gateway or local validation (never on timeout). */
    FAILED,

    /** User abandoned the gateway checkout before verification. */
    CANCELLED,

    /** Successful payment whose refund was recorded administratively (MVP: record-only). */
    REFUNDED;

    public boolean isTerminal() {
        return this == FAILED || this == CANCELLED || this == REFUNDED;
    }

    /** In-flight states that block creating another attempt for the same business operation. */
    public boolean isInFlight() {
        return this == PENDING || this == VERIFYING || this == UNKNOWN;
    }

    /**
     * Allowed transitions of the approved state machine. Any transition not listed here is
     * rejected by {@code PaymentTransaction} and, for the critical invariants, additionally by
     * database constraints (partial unique indexes on SUCCESS and in-flight rows).
     */
    public boolean canTransitionTo(PaymentStatus next) {
        if (next == null || next == this) {
            return false;
        }
        return switch (this) {
            case PENDING -> next == VERIFYING || next == SUCCESS || next == UNKNOWN
                    || next == FAILED || next == CANCELLED;
            case VERIFYING -> next == SUCCESS || next == UNKNOWN || next == FAILED || next == CANCELLED;
            // UNKNOWN is deliberately non-terminal: only ever resolved forward/backward to
            // VERIFYING (retry), or settled by a definitive gateway answer.
            case UNKNOWN -> next == VERIFYING || next == PENDING || next == SUCCESS
                    || next == FAILED || next == CANCELLED;
            // SUCCESS is immutable against failure/cancel: a duplicate or delayed callback must
            // never demote a verified payment. Only a recorded refund moves it on.
            case SUCCESS -> next == REFUNDED;
            case FAILED, CANCELLED, REFUNDED -> false;
        };
    }
}
