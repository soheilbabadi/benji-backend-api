package social.benji.benji_backend_api.usermanagement.domain;

/**
 * Single-role model for MVP. Future Expert/Admin modules reuse this column
 * rather than introducing separate identity tables.
 */
public enum UserRole {
    USER,
    EXPERT,
    ADMIN
}
