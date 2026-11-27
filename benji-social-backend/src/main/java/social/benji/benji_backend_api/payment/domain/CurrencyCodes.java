package social.benji.benji_backend_api.payment.domain;

/**
 * Single source of truth for the currency used by the Payment module.
 * Approved decision D: all money values are IRR (Iranian Rial), integer minor units,
 * end-to-end (tariff → transaction snapshot → ZarinPal vNext {@code amount}, which expects Rials).
 * Toman is a presentation-layer concern only and must never be persisted or sent to the gateway.
 */
public final class CurrencyCodes {

    public static final String IRR = "IRR";

    private CurrencyCodes() {
    }
}
