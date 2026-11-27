package social.benji.benji_backend_api.payment.domain.valueobject;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {
    @Test
    void convertsTomansToRialsWithoutFloatingPoint() {
        Money money = Money.fromTomans(new BigDecimal("1250"));
        assertEquals(new BigDecimal("12500"), money.amount());
        assertEquals(new BigDecimal("1250"), money.toTomans());
        assertEquals("IRR", money.currency());
    }

    @Test
    void rejectsInvalidCurrencyAndZeroAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> new Money(BigDecimal.ONE, "USD"));
        assertThrows(IllegalArgumentException.class,
                () -> Money.fromRials(BigDecimal.ZERO));
    }
}
