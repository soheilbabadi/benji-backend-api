package social.benji.benji_backend_api.lostfound.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import social.benji.benji_backend_api.lostfound.domain.AdvertisementReportEntity;
import social.benji.benji_backend_api.lostfound.domain.ReportStatus;

import java.util.Optional;
import java.util.UUID;

public interface AdvertisementReportRepository extends JpaRepository<AdvertisementReportEntity, UUID> {

    boolean existsByAdvertisementIdAndReporterUserIdAndStatus(UUID advertisementId, UUID reporterUserId, ReportStatus status);

    Page<AdvertisementReportEntity> findByStatusOrderByCreatedAtAsc(ReportStatus status, Pageable pageable);

    Optional<AdvertisementReportEntity> findFirstByAdvertisementIdAndReporterUserIdAndStatus(
            UUID advertisementId, UUID reporterUserId, ReportStatus status);
}
