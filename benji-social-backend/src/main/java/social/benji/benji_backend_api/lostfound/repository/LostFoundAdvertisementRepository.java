package social.benji.benji_backend_api.lostfound.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import social.benji.benji_backend_api.lostfound.domain.AdvertisementStatus;
import social.benji.benji_backend_api.lostfound.domain.AdvertisementType;
import social.benji.benji_backend_api.lostfound.domain.LostFoundAdvertisementEntity;
import social.benji.benji_backend_api.lostfound.domain.Species;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LostFoundAdvertisementRepository extends JpaRepository<LostFoundAdvertisementEntity, UUID> {

    Optional<LostFoundAdvertisementEntity> findByPublicId(UUID publicId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from LostFoundAdvertisementEntity a where a.publicId = :publicId")
    Optional<LostFoundAdvertisementEntity> findByPublicIdForUpdate(@Param("publicId") UUID publicId);

    Page<LostFoundAdvertisementEntity> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /** Scheduler: PUBLISHED ads whose exact 30 x 24-hour window has elapsed. */
    List<LostFoundAdvertisementEntity> findByStatusAndExpiresAtBefore(AdvertisementStatus status, Instant now);

    List<LostFoundAdvertisementEntity> findByStatusAndPaymentDeadlineAtBefore(
            AdvertisementStatus status, Instant now);

    boolean existsByPetIdAndStatus(UUID petId, AdvertisementStatus status);

    /**
     * Public search over currently PUBLISHED advertisements only.
     * All filters are optional; text search uses case-insensitive LIKE on public fields — justified
     * for MVP volumes without introducing Elasticsearch. city is matched case-insensitively and
     * prefix-wise to tolerate minor spelling variants.
     */
    @Query("""
           select a from LostFoundAdvertisementEntity a
            where a.includeInSearch = true
              and a.status in :visibleStatuses
               and a.expiresAt is not null
               and a.expiresAt > :now
              and (:type is null or a.advertisementType = :type)
              and (:species is null or a.species = :species)
              and (:city is null or lower(a.city) like concat(lower(:city), '%'))
              and (:area is null or lower(coalesce(a.area,'')) like concat('%', lower(coalesce(:area,'')), '%'))
              and (:breedId is null or a.breedId = :breedId)
              and (:sex is null or a.sex = :sex)
              and (:from is null or a.incidentAt >= :from)
              and (:to is null or a.incidentAt <= :to)
              and (:text is null or lower(a.description) like concat('%', lower(:text), '%')
                                or lower(coalesce(a.animalName,'')) like concat('%', lower(:text), '%')
                                or lower(coalesce(a.color,'')) like concat('%', lower(:text), '%')
                                or lower(coalesce(a.distinguishingCharacteristics,'')) like concat('%', lower(:text), '%'))
           """)
    Page<LostFoundAdvertisementEntity> searchPublic(
            @Param("visibleStatuses") List<AdvertisementStatus> visibleStatuses,
            @Param("now") Instant now,
            @Param("type") AdvertisementType type,
            @Param("species") Species species,
            @Param("city") String city,
            @Param("area") String area,
            @Param("breedId") UUID breedId,
            @Param("sex") social.benji.benji_backend_api.lostfound.domain.AnimalSex sex,
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("text") String text,
            Pageable pageable);
}
