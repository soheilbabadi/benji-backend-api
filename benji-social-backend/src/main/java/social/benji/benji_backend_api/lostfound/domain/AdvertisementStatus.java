package social.benji.benji_backend_api.lostfound.domain;

public enum AdvertisementStatus {
    UNDER_REVIEW,
    REJECTED,
    PAYMENT_PENDING,
    PUBLISHED,
    EXPIRED,
    DEACTIVATED;

    /** Compatibility projection of the independent lifecycle state. */
    public boolean isPubliclyVisible() {
        return this == PUBLISHED;
    }

    /** Compatibility projection: only rejected advertisements are owner-editable. */
    public boolean isEditable() {
        return this == REJECTED;
    }
}
