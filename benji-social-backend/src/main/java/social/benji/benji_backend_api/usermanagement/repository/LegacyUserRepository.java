package social.benji.benji_backend_api.usermanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import social.benji.benji_backend_api.usermanagement.model.LegacyUser;

import java.util.Optional;

/**
 * Legacy email-based repository for {@link LegacyUser}. The canonical
 * user-management repository is {@link UserRepository} (UUID-keyed identity root).
 */
@Repository
public interface LegacyUserRepository extends JpaRepository<LegacyUser, String> {
    Optional<LegacyUser> findByEmail(String email);
    Optional<LegacyUser> findByMobileNumber(String mobileNumber);
    boolean existsByEmail(String email);
    boolean existsByMobileNumber(String mobileNumber);
}
