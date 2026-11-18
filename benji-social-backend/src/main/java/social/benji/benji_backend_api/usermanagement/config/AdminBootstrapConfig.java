package social.benji.benji_backend_api.usermanagement.config;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import social.benji.benji_backend_api.usermanagement.domain.User;
import social.benji.benji_backend_api.usermanagement.domain.UserRole;
import social.benji.benji_backend_api.usermanagement.domain.UserStatus;
import social.benji.benji_backend_api.usermanagement.repository.UserRepository;

/**
 * Bootstraps the default administrator account so the system is operable from
 * the very first start: without it nobody could reach the admin-only role
 * endpoints to grant roles to anyone else.
 *
 * <p>The runner is idempotent and safe on every profile:</p>
 * <ul>
 *   <li>If any active ADMIN row already exists, nothing happens.</li>
 *   <li>Otherwise a user with the configured mobile number is created (or an
 *       existing row with that number is promoted to ADMIN).</li>
 *   <li>Disable entirely with {@code app.admin-bootstrap.enabled=false}.</li>
 * </ul>
 *
 * <p>The admin signs in through the normal OTP flow (code is sent via the
 * configured SMS port), exactly like every other account — there is no
 * password to seed. The mobile number must be a valid E.164 value because the
 * column participates in a unique identity constraint.</p>
 */
@Slf4j
@Configuration
public class AdminBootstrapConfig {

    @Bean
    ApplicationRunner defaultAdminSeeder(
            UserRepository userRepository,
            @Value("${app.admin-bootstrap.enabled:true}") boolean enabled,
            @Value("${app.admin-bootstrap.mobile-number:+989120000000}") String mobileNumber,
            @Value("${app.admin-bootstrap.username:admin}") String username,
            @Value("${app.admin-bootstrap.first-name:Benji}") String firstName,
            @Value("${app.admin-bootstrap.last-name:Admin}") String lastName,
            @Value("${app.admin-bootstrap.email:admin@benji.social}") String email) {

        return args -> seedIfAbsent(userRepository, enabled, mobileNumber,
                username, firstName, lastName, email);
    }

    @Transactional
    void seedIfAbsent(UserRepository userRepository, boolean enabled, String mobileNumber,
                      String username, String firstName, String lastName, String email) {
        if (!enabled) {
            log.info("admin bootstrap disabled via app.admin-bootstrap.enabled=false");
            return;
        }

        // 1. Someone is already in charge — leave the roster untouched.
        Optional<User> existingAdmin = userRepository.findAll().stream()
                .filter(User::isActive)
                .filter(u -> u.getRole() == UserRole.ADMIN)
                .findFirst();
        if (existingAdmin.isPresent()) {
            log.debug("active admin already present (id={}); skipping bootstrap",
                    existingAdmin.get().getId());
            return;
        }

        // 2. Reuse the configured mobile's live row if the owner already registered.
        Optional<User> existingByMobile = userRepository.findLiveByMobile(mobileNumber);
        User admin;
        if (existingByMobile.isPresent()) {
            admin = existingByMobile.get();
            admin.setRole(UserRole.ADMIN);
            if (admin.getUsername() == null) {
                admin.setUsername(username);
            }
            userRepository.save(admin);
            log.info("existing user {} promoted to default ADMIN", admin.getId());
            return;
        }

        // 3. Guard against colliding with a tombstoned (soft-deleted) row that
        //    still physically holds the number/username in its unique columns.
        boolean tombstoneCollision = userRepository.findAll().stream()
                .anyMatch(u -> mobileNumber.equals(u.getMobileNumber())
                        || username.equalsIgnoreCase(String.valueOf(u.getUsername())));
        if (tombstoneCollision) {
            log.warn("default admin not seeded: mobile {} or username '{}' collides with a "
                    + "deleted row; set app.admin-bootstrap.mobile-number to another value",
                    mobileNumber, username);
            return;
        }

        admin = User.builder()
                .mobileNumber(mobileNumber)
                .mobileVerified(true)
                .email(email.toLowerCase())
                .emailVerified(true)
                .firstName(firstName)
                .lastName(lastName)
                .username(username.toLowerCase())
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();
        try {
            userRepository.saveAndFlush(admin);
            log.info("default ADMIN user created: id={} mobile={} username={}",
                    admin.getId(), admin.getMobileNumber(), admin.getUsername());
        } catch (RuntimeException e) {
            // Never take the application down over a seeding conflict; an
            // operator can fix the configuration and restart.
            log.error("default admin seeding failed: {}", e.getMessage());
        }
    }
}
