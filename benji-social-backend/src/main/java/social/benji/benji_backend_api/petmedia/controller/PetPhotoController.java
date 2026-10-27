package social.benji.benji_backend_api.petmedia.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import social.benji.benji_backend_api.petmedia.application.PhotoService;
import social.benji.benji_backend_api.petmedia.domain.PetAlbumMedia;
import social.benji.benji_backend_api.petmedia.dto.AccessUrlResponse;
import social.benji.benji_backend_api.petmedia.dto.MovePhotoRequest;
import social.benji.benji_backend_api.petmedia.dto.PhotoResponse;
import social.benji.benji_backend_api.usermanagement.security.JwtAuthenticationFilter.UserPrincipal;

/**
 * Thin REST layer for photos inside pet albums. Bytes are handled as multipart
 * uploads (validated/normalized server-side by PhotoService); clients never
 * receive object keys — image access goes through short-lived pre-signed URLs.
 * Ownership is always derived from the authenticated JWT principal.
 */
@RestController
@RequestMapping("/api/v1/pets/{petId}/albums/{albumId}/photos")
public class PetPhotoController {

    private final PhotoService photoService;

    public PetPhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }

    /** Multipart upload; up to 20 files per request, each validated + normalized. */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<PhotoResponse>> upload(@PathVariable UUID petId,
                                                      @PathVariable UUID albumId,
                                                      @RequestPart("files") List<MultipartFile> files,
                                                      @AuthenticationPrincipal UserPrincipal principal) {
        List<PetAlbumMedia> saved = photoService.uploadPhotos(petId, albumId, principal.id(), files);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(saved.stream().map(PhotoResponse::from).toList());
    }

    @GetMapping
    public List<PhotoResponse> list(@PathVariable UUID petId,
                                    @PathVariable UUID albumId,
                                    @AuthenticationPrincipal UserPrincipal principal) {
        return photoService.listPhotos(petId, albumId, principal.id()).stream()
                .map(PhotoResponse::from)
                .toList();
    }

    /** Short-lived pre-signed GET URL; the bucket policy stays private. */
    @GetMapping("/{photoId}/url")
    public AccessUrlResponse accessUrl(@PathVariable UUID petId,
                                       @PathVariable UUID albumId,
                                       @PathVariable UUID photoId,
                                       @AuthenticationPrincipal UserPrincipal principal) {
        String url = photoService.getAccessUrl(petId, albumId, photoId, principal.id());
        return new AccessUrlResponse(url);
    }

    @DeleteMapping("/{photoId}")
    public ResponseEntity<HttpStatus> delete(@PathVariable UUID petId,
                                       @PathVariable UUID albumId,
                                       @PathVariable UUID photoId,
                                       @AuthenticationPrincipal UserPrincipal principal) {
        photoService.deletePhoto(petId, albumId, photoId, principal.id());
        return ResponseEntity.noContent().build();
    }

    /** Logical move only: re-points the DB row; the MinIO object keeps its key. */
    @PutMapping("/{photoId}/move")
    public PhotoResponse move(@PathVariable UUID petId,
                              @PathVariable UUID albumId,
                              @PathVariable UUID photoId,
                              @Valid @RequestBody MovePhotoRequest request,
                              @AuthenticationPrincipal UserPrincipal principal) {
        PetAlbumMedia moved = photoService.movePhoto(petId, albumId, photoId,
                request.targetAlbumId(), principal.id());
        return PhotoResponse.from(moved);
    }
}
