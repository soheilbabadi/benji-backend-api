package social.benji.benji_backend_api.petmedia.web.dto;

/** Short-lived pre-signed URL; expires server-side within minutes. */
public record AccessUrlResponse(String url) {
}
