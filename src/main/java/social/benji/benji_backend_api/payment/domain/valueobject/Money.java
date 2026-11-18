package social.benji.benji_backend_api.payment.domain.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Money(BigDecimal amount, String currency) {
    public static final String IRR = "IRR";
    private static final BigDecimal RIALS_PER_TOMAN = BigDecimal.TEN;

    public Money {
        Objects.requireNonNull(amount, "Amount is required");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (!IRR.equals(currency)) {
            throw new IllegalArgumentException("Only IRR is supported");
        }
    }

    public static Money fromRials(BigDecimal rials) {
        return new Money(rials.setScale(0, RoundingMode.UNNECESSARY), IRR);
    }

    public static Money fromTomans(BigDecimal tomans) {
        return fromRials(tomans.multiply(RIALS_PER_TOMAN).setScale(0, RoundingMode.UNNECESSARY));
    }

    public BigDecimal toTomans() {
        return amount.divide(RIALS_PER_TOMAN, 0, RoundingMode.UNNECESSARY);
    }

    public long toRialMinorUnits() {
        return amount.longValueExact();
    }
}
