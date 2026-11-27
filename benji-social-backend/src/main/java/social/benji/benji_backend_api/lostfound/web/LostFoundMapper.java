package social.benji.benji_backend_api.lostfound.web;

import org.springframework.stereotype.Component;
import social.benji.benji_backend_api.lostfound.domain.LostFoundAdvertisementEntity;
import social.benji.benji_backend_api.lostfound.repository.AdvertisementImageRepository;
import social.benji.benji_backend_api.lostfound.web.dto.LostFoundDtos.*;

import java.util.List;

/** Entity → DTO conversion. Public DTOs always mask the phone and drop owner/pet identifiers. */
@Component
class LostFoundMapper {

    private final AdvertisementImageRepository images;

    LostFoundMapper(AdvertisementImageRepository images) {
        this.images = images;
    }

    PublicAdvertisementDto toPublic(LostFoundAdvertisementEntity a) {
        return new PublicAdvertisementDto(
                a.getPublicId(), a.getAdvertisementType(), a.getStatus(), a.getSpecies(), a.getBreedId(),
                a.getAnimalName(), a.getSex(), a.getColor(), a.getDistinguishingCharacteristics(),
                a.getDescription(), a.getIncidentAt(), a.getCountry(), a.getCity(), a.getArea(),
                a.getLatitude(), a.getLongitude(), a.getLocationPrecision(), a.getContactMethod(),
                a.maskedContactPhone(), a.getRewardAmountIrr(),
                a.getPublishedAt(), a.getExpiresAt(), a.getResolvedAt(),
                imageDtos(a.getId()));
    }

    ManagedAdvertisementDto toManaged(LostFoundAdvertisementEntity a) {
        return new ManagedAdvertisementDto(
                a.getPublicId(), a.getAdvertisementType(), a.getStatus(), a.getPetId(), a.getSpecies(),
                a.getBreedId(), a.getAnimalName(), a.getSex(), a.getColor(),
                a.getDistinguishingCharacteristics(), a.getDescription(), a.getIncidentAt(),
                a.getCountry(), a.getCity(), a.getArea(), a.getLatitude(), a.getLongitude(),
                a.getLocationPrecision(), a.getContactMethod(), a.getContactPhoneE164(),
                a.getRewardAmountIrr(), a.isIncludeInSearch(), a.getPublishedAt(), a.getExpiresAt(),
                a.getResolvedAt(), a.getClosedAt(), a.getCreatedAt(), a.getUpdatedAt(), a.getVersion(),
                imageDtos(a.getId()));
    }

    private List<ImageDto> imageDtos(java.util.UUID advertisementId) {
        return images.findByAdvertisementIdOrderBySortOrderAsc(advertisementId).stream()
                .map(i -> new ImageDto(i.getId(), i.getContentType(), i.getSizeBytes(), i.getSortOrder()))
                .toList();
    }

    LostFoundAdvertisementEntity fromCreate(CreateLostRequest r) {
        return LostFoundAdvertisementEntity.builder()
                .petId(r.petId()).species(r.species()).breedId(r.breedId()).animalName(r.animalName())
                .sex(r.sex() != null ? r.sex() : social.benji.benji_backend_api.lostfound.domain.AnimalSex.UNKNOWN)
                .color(r.color()).distinguishingCharacteristics(r.distinguishingCharacteristics())
                .description(r.description()).incidentAt(r.incidentAt()).country(r.country()).city(r.city())
                .area(r.area()).latitude(r.latitude()).longitude(r.longitude())
                .locationPrecision(r.locationPrecision() != null ? r.locationPrecision()
                        : social.benji.benji_backend_api.lostfound.domain.LocationPrecision.CITY)
                .contactMethod(r.contactMethod()).contactPhoneE164(r.contactPhoneE164())
                .rewardAmountIrr(r.rewardAmountIrr())
                .build();
    }

    LostFoundAdvertisementEntity fromCreate(CreateFoundRequest r) {
        return LostFoundAdvertisementEntity.builder()
                .species(r.species()).breedId(r.breedId()).animalName(r.animalName())
                .sex(r.sex() != null ? r.sex() : social.benji.benji_backend_api.lostfound.domain.AnimalSex.UNKNOWN)
                .color(r.color()).distinguishingCharacteristics(r.distinguishingCharacteristics())
                .description(r.description()).incidentAt(r.incidentAt()).country(r.country()).city(r.city())
                .area(r.area()).latitude(r.latitude()).longitude(r.longitude())
                .locationPrecision(r.locationPrecision() != null ? r.locationPrecision()
                        : social.benji.benji_backend_api.lostfound.domain.LocationPrecision.CITY)
                .contactMethod(r.contactMethod()).contactPhoneE164(r.contactPhoneE164())
                .build();
    }
}
