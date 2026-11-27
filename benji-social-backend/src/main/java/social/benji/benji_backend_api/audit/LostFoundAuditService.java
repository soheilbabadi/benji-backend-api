package social.benji.benji_backend_api.audit;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.stereotype.Service;
import social.benji.benji_backend_api.lostfound.domain.LostFoundAdvertisementEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class LostFoundAuditService {
    @PersistenceContext
    private EntityManager entityManager;

    public List<AuditRevisionDto> revisionsForPublicAdvertisement(UUID publicId) {
        AuditReader reader = AuditReaderFactory.get(entityManager);
        List<?> revisionsOfEntity = reader.createQuery()
                .forRevisionsOfEntity(LostFoundAdvertisementEntity.class, false, true)
                .add(AuditEntity.property("publicId").eq(publicId))
                .setMaxResults(50)
                .getResultList();

        List<Number> revisionIds = new ArrayList<>();
        for (Object row : revisionsOfEntity) {
            Object revisionId = row;
            if (row instanceof Object[] columns && columns.length >= 2) {
                revisionId = columns[1] instanceof BenjiRevisionEntity revision
                        ? revision.getRevisionId()
                        : columns[1];
            }
            if (revisionId instanceof Number number) {
                revisionIds.add(number);
            }
        }

        List<AuditRevisionDto> result = new ArrayList<>();
        for (Number revisionId : revisionIds) {
            BenjiRevisionEntity revision =
                    reader.findRevision(BenjiRevisionEntity.class, revisionId);
            if (revision != null) {
                result.add(new AuditRevisionDto(
                        revision.getRevisionId(),
                        revision.getRevisionTimestamp(),
                        revision.getActorUserId(),
                        revision.getActorType(),
                        revision.getCorrelationId()));
            }
        }
        return result;
    }

    public record AuditRevisionDto(
            int revisionId,
            java.time.Instant revisionTimestamp,
            UUID actorUserId,
            BenjiActorType actorType,
            String correlationId) {}
}
