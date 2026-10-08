package social.benji.benji_backend_api.usermanagement.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import social.benji.benji_backend_api.usermanagement.domain.OtpChallenge;

public interface OtpChallengeRepository extends JpaRepository<OtpChallenge, UUID> {

    @Query("""
            select c from OtpChallenge c
            where c.mobileNumber = :mobile and c.status = social.benji.benji_backend_api.usermanagement.domain.OtpChallengeStatus.CREATED
            order by c.createdAt desc
            limit 1
            """)
    Optional<OtpChallenge> findLatestActive(@Param("mobile") String mobile);

    /** Timestamp of the most recent request for this number, whatever its state. */
    @Query("""
            select max(c.createdAt) from OtpChallenge c
            where c.mobileNumber = :mobile
            """)
    Optional<Instant> findLatestCreatedAt(@Param("mobile") String mobile);

    @Query("""
            select count(c) from OtpChallenge c
            where c.mobileNumber = :mobile and c.createdAt > :since
            """)
    long countRequestsSince(@Param("mobile") String mobile, @Param("since") Instant since);

    @Modifying
    @Query("""
            update OtpChallenge c
            set c.status = social.benji.benji_backend_api.usermanagement.domain.OtpChallengeStatus.BLOCKED
            where c.mobileNumber = :mobile
              and c.status = social.benji.benji_backend_api.usermanagement.domain.OtpChallengeStatus.CREATED
            """)
    int supersedeActive(@Param("mobile") String mobile);

    /** Housekeeping for stale rows; called opportunistically, not on the hot path. */
    @Modifying
    @Query("""
            delete from OtpChallenge c
            where c.expiresAt < :before
              and c.status <> social.benji.benji_backend_api.usermanagement.domain.OtpChallengeStatus.CREATED
            """)
    int deleteResolvedOlderThan(@Param("before") Instant before);
}
