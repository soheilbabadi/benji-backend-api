package social.benji.benji_backend_api.consultation.domain.valueobject;

import java.time.Instant;
import java.util.Currency;
import java.util.Objects;

/**
 * Value object representing a monetary amount.
 * Uses cents as the internal representation to avoid floating-point issues.
 */
public record Money(Currency currency, long amountInCents) {

    public Money {
        Objects.requireNonNull(currency, "Currency cannot be null");
        if (amountInCents < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
    }

    public static Money of(long amountInCents, String currencyCode) {
        return new Money(Currency.getInstance(currencyCode), amountInCents);
    }

    public static Money usd(long amountInCents) {
        return new Money(Currency.getInstance("USD"), amountInCents);
    }

    public boolean isZero() {
        return amountInCents == 0;
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add money with different currencies");
        }
        return new Money(this.currency, this.amountInCents + other.amountInCents);
    }
}
