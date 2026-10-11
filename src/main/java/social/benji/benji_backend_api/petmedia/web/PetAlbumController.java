package social.benji.benji_backend_api.petmedia.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import social.benji.benji_backend_api.petmedia.application.AlbumService;
import social.benji.benji_backend_api.petmedia.domain.PetAlbum;
import social.benji.benji_backend_api.petmedia.web.dto.AlbumResponse;
import social.benji.benji_backend_api.petmedia.web.dto.CreateAlbumRequest;
import social.benji.benji_backend_api.petmedia.web.dto.SetCoverRequest;
import social.benji.benji_backend_api.petmedia.web.dto.UpdateAlbumRequest;
import social.benji.benji_backend_api.usermanagement.security.JwtAuthenticationFilter.UserPrincipal;

/**
 * Thin REST layer for pet albums. The authenticated JWT principal — never a
 * client-provided owner id — determines ownership; all rules (User -> Pet ->
 * Album chain) live in the services via OwnershipGuard. Every endpoint
 * requires a valid bearer token (JwtAuthenticationFilter + SecurityConfig).
 */
@RestController
@RequestMapping("/api/v1/pets/{petId}/albums")
public class PetAlbumController {

    private final AlbumService albumService;

    public PetAlbumController(AlbumService albumService) {
        this.albumService = albumService;
    }

    @PostMapping
    public ResponseEntity<AlbumResponse> create(@PathVariable UUID petId,
                                                @Valid @RequestBody CreateAlbumRequest request,
                                                @AuthenticationPrincipal UserPrincipal principal) {
        PetAlbum album = albumService.createAlbum(principal.id(), petId, request.name(), request.description());
        return ResponseEntity.status(HttpStatus.CREATED).body(AlbumResponse.from(album));
    }

    @GetMapping
    public List<AlbumResponse> list(@PathVariable UUID petId,
                                    @AuthenticationPrincipal UserPrincipal principal) {
        return albumService.listAlbums(petId, principal.id()).stream()
                .map(AlbumResponse::from)
                .toList();
    }

    @GetMapping("/{albumId}")
    public AlbumResponse get(@PathVariable UUID petId,
                             @PathVariable UUID albumId,
                             @AuthenticationPrincipal UserPrincipal principal) {
        return AlbumResponse.from(albumService.getAlbum(petId, albumId, principal.id()));
    }

    /** Partial update: null fields are left untouched. */
    @PatchMapping("/{albumId}")
    public AlbumResponse update(@PathVariable UUID petId,
                                @PathVariable UUID albumId,
                                @Valid @RequestBody UpdateAlbumRequest request,
                                @AuthenticationPrincipal UserPrincipal principal) {
        PetAlbum album = albumService.updateAlbum(petId, albumId, principal.id(),
                request.name(), request.description());
        return AlbumResponse.from(album);
    }

    /** Hard delete: removes every photo object from MinIO and its DB row first. */
    @DeleteMapping("/{albumId}")
    public ResponseEntity<HttpStatus> delete(@PathVariable UUID petId,
                                       @PathVariable UUID albumId,
                                       @AuthenticationPrincipal UserPrincipal principal) {
        albumService.deleteAlbum(petId, albumId, principal.id());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{albumId}/cover")
    public AlbumResponse setCover(@PathVariable UUID petId,
                                  @PathVariable UUID albumId,
                                  @Valid @RequestBody SetCoverRequest request,
                                  @AuthenticationPrincipal UserPrincipal principal) {
        PetAlbum album = albumService.setCover(petId, albumId, principal.id(), request.photoId());
        return AlbumResponse.from(album);
    }
}
