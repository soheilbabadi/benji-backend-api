package social.benji.benji_backend_api.usermanagement.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import social.benji.benji_backend_api.usermanagement.domain.User;
import social.benji.benji_backend_api.usermanagement.domain.exception.UserManagementErrors;
import social.benji.benji_backend_api.usermanagement.repository.UserRepository;

@Service
public class DefaultUserService implements UserService {

    private final UserRepository userRepository;

    public DefaultUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(java.util.UUID id) {
        return userRepository.findById(id)
                .filter(User::isActive)
                .orElseThrow(UserManagementErrors.accountUnavailable());
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Optional<User> findByMobile(String normalizedMobile) {
        return userRepository.findLiveByMobile(normalizedMobile);
    }

    @Override
    @Transactional
    public User registerWithVerifiedMobile(String normalizedMobile) {
        // Concurrent registrations for the same number are settled by the DB unique
        // constraint; the loser simply adopts the winner's row.
        try {
            return userRepository.saveAndFlush(User.newActive(normalizedMobile));
        } catch (DataIntegrityViolationException e) {
            return userRepository.findLiveByMobile(normalizedMobile)
                    .orElseThrow(UserManagementErrors::authenticationFailed);
        }
    }

    @Override
    @Transactional
    public User updateProfile(java.util.UUID userId, String firstName, String lastName, String email) {
        User user = findById(userId);
        if (firstName != null) {
            user.setFirstName(firstName.isBlank() ? null : firstName.trim());
        }
        if (lastName != null) {
            user.setLastName(lastName.isBlank() ? null : lastName.trim());
        }
        if (email != null && !email.isBlank()) {
            String normalizedEmail = email.trim().toLowerCase();
            if (!normalizedEmail.equals(user.getEmail())
                    && userRepository.existsByEmailAndDeletedAtIsNull(normalizedEmail)) {
                throw UserManagementErrors.emailAlreadyInUse();
            }
            user.setEmail(normalizedEmail);
            // Ownership of an address is only proven through a future verification flow.
            user.setEmailVerified(false);
        }
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void softDelete(java.util.UUID userId) {
        findById(userId);
        userRepository.softDeleteById(userId, java.time.Instant.now());
    }
}
