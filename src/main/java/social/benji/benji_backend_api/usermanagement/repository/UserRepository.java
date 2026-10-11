package social.benji.benji_backend_api.usermanagement.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import social.benji.benji_backend_api.usermanagement.domain.User;

public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Lookup by live mobile number. Deleted accounts release their number, so
     * only non-deleted rows may claim an identity.
     */
    @Query("select u from User u where u.mobileNumber = :mobile and u.deletedAt is null")
    Optional<User> findLiveByMobile(@Param("mobile") String mobile);

    boolean existsByEmailAndDeletedAtIsNull(String email);

    /**
     * Soft delete that also releases the mobile number for future re-registration:
     * the live unique identity moves to a tombstone value derived from the row id.
     */
    @Modifying
    @Query("""
            update User u
            set u.deletedAt = :now, u.updatedAt = :now,
                u.mobileNumber = 'deleted-' || cast(u.id as string)
            where u.id = :id and u.deletedAt is null
            """)
    int softDeleteById(@Param("id") UUID id, @Param("now") Instant now);
}
