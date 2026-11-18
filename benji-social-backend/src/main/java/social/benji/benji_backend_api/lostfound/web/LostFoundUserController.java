package social.benji.benji_backend_api.lostfound.web;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import social.benji.benji_backend_api.exception.BenjiCustomException;
import social.benji.benji_backend_api.lostfound.domain.AdvertisementImageEntity;
import social.benji.benji_backend_api.lostfound.domain.LostFoundAdvertisementEntity;
import social.benji.benji_backend_api.lostfound.service.AdvertisementImageService;
import social.benji.benji_backend_api.lostfound.service.LostFoundAdvertisementService;
import social.benji.benji_backend_api.lostfound.web.dto.LostFoundDtos.*;
import social.benji.benji_backend_api.usermanagement.security.JwtAuthenticationFilter.UserPrincipal;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Authenticated advertiser endpoints. The acting user id comes ONLY from the JWT principal —
 * never from a request body or query param. There is deliberately NO endpoint that sets status
 * to ACTIVE: activation happens exclusively via the verified payment event.
 */
@RestController
@RequestMapping("/api/v1/lost-found")
class LostFoundUserController {

    private final LostFoundAdvertisementService service;
    private final AdvertisementImageService imageService;
    private final LostFoundMapper mapper;

    LostFoundUserController(LostFoundAdvertisementService service,
                            AdvertisementImageService imageService,
                            LostFoundMapper mapper) {
        this.service = service;
        this.imageService = imageService;
        this.mapper = mapper;
    }

    @PostMapping("/ads/lost")
    public ResponseEntity<ManagedAdvertisementDto> createLost(
            @Valid @RequestBody CreateLostRequest body,
            @AuthenticationPrincipal UserPrincipal me) {
        LostFoundAdvertisementEntity ad = service.createLost(me.id(), mapper.fromCreate(body));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toManaged(ad));
    }

    @PostMapping("/ads/found")
    public ResponseEntity<ManagedAdvertisementDto> createFound(
            @Valid @RequestBody CreateFoundRequest body,
            @AuthenticationPrincipal UserPrincipal me) {
        LostFoundAdvertisementEntity ad = service.createFound(me.id(), mapper.fromCreate(body));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toManaged(ad));
    }

    @GetMapping("/ads")
    public Page<ManagedAdvertisementDto> myAds(@AuthenticationPrincipal UserPrincipal me,
                                               @PageableDefault(size = 20) Pageable pageable) {
        return service.myAdvertisements(me.id(), pageable).map(mapper::toManaged);
    }

    @GetMapping("/ads/{publicId}")
    public ManagedAdvertisementDto getMyAd(@PathVariable UUID publicId,
                                           @AuthenticationPrincipal UserPrincipal me) {
        return mapper.toManaged(service.getManaged(me.id(), publicId));
    }

    /** Edit allowed only in DRAFT / PAYMENT_PENDING; type, pet link and status are immutable here. */
    @PatchMapping("/ads/{publicId}")
    public ManagedAdvertisementDto update(@PathVariable UUID publicId,
                                          @Valid @RequestBody UpdateAdvertisementRequest body,
                                          @AuthenticationPrincipal UserPrincipal me) {
        LostFoundAdvertisementEntity ad = service.updateDraft(me.id(), publicId, a -> {
            if (body.animalName() != null) a.setAnimalName(body.animalName());
            if (body.sex() != null) a.setSex(body.sex());
            if (body.color() != null) a.setColor(body.color());
            if (body.distinguishingCharacteristics() != null) a.setDistinguishingCharacteristics(body.distinguishingCharacteristics());
            if (body.description() != null) a.setDescription(body.description());
            if (body.incidentAt() != null) a.setIncidentAt(body.incidentAt());
            if (body.country() != null) a.setCountry(body.country());
            if (body.city() != null) a.setCity(body.city());
            if (body.area() != null) a.setArea(body.area());
            if (body.latitude() != null) a.setLatitude(body.latitude());
            if (body.longitude() != null) a.setLongitude(body.longitude());
            if (body.locationPrecision() != null) a.setLocationPrecision(body.locationPrecision());
            if (body.contactMethod() != null) a.setContactMethod(body.contactMethod());
            if (body.contactPhoneE164() != null) a.setContactPhoneE164(body.contactPhoneE164());
            if (body.rewardAmountIrr() != null) a.setRewardAmountIrr(body.rewardAmountIrr());
        });
        return mapper.toManaged(ad);
    }

    /** LOST only: DRAFT -> PAYMENT_PENDING. Client receives the ad id to hand to the payment flow. */
    @PostMapping("/ads/{publicId}/submit-for-payment")
    public ManagedAdvertisementDto submitForPayment(@PathVariable UUID publicId,
                                                    @AuthenticationPrincipal UserPrincipal me) {
        return mapper.toManaged(service.submitForPayment(me.id(), publicId));
    }

    /** Mark the animal as found/reunited. RESOLVED IS TERMINAL — no reactivation endpoint exists. */
    @PostMapping("/ads/{publicId}/resolve")
    public ManagedAdvertisementDto resolve(@PathVariable UUID publicId,
                                           @Valid @RequestBody ResolveRequest body,
                                           @AuthenticationPrincipal UserPrincipal me) {
        return mapper.toManaged(service.resolve(me.id(), publicId, body.note()));
    }

    @PostMapping("/ads/{publicId}/close")
    public ManagedAdvertisementDto close(@PathVariable UUID publicId,
                                         @AuthenticationPrincipal UserPrincipal me) {
        return mapper.toManaged(service.cancelOrClose(me.id(), publicId));
    }

    // ---- Images (max 5 × 5MB, JPEG/PNG/WebP, private MinIO storage) ---------------------------

    @PostMapping(value = "/ads/{publicId}/images", consumes = "multipart/form-data")
    public ResponseEntity<ImageDto> uploadImage(@PathVariable UUID publicId,
                                                @RequestParam("file") MultipartFile file,
                                                @AuthenticationPrincipal UserPrincipal me) {
        try {
            AdvertisementImageEntity img = imageService.upload(
                    me.id(), publicId, file.getContentType(), file.getBytes());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ImageDto(img.getId(), img.getContentType(), img.getSizeBytes(), img.getSortOrder()));
        } catch (IOException e) {
            throw new BenjiCustomException.ApplicationServerException("Could not read upload: " + e.getMessage());
        }
    }

    @DeleteMapping("/ads/{publicId}/images/{imageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable UUID publicId,
                                            @PathVariable UUID imageId,
                                            @AuthenticationPrincipal UserPrincipal me) {
        imageService.delete(me.id(), publicId, imageId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ads/{publicId}/images")
    public List<ImageDto> listImages(@PathVariable UUID publicId,
                                     @AuthenticationPrincipal UserPrincipal me) {
        service.getManaged(me.id(), publicId); // ownership gate before listing
        return imageService.list(publicId).stream()
                .map(i -> new ImageDto(i.getId(), i.getContentType(), i.getSizeBytes(), i.getSortOrder()))
                .toList();
    }
}
