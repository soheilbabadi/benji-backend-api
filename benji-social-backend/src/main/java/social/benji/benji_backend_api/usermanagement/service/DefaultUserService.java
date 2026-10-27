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
    private final UserAvatarService userAvatarService;

    public DefaultUserService(UserRepository userRepository, UserAvatarService userAvatarService) {
        this.userRepository = userRepository;
        this.userAvatarService = userAvatarService;
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
        boolean createdHere = false;
        User user;
        try {
            user = userRepository.saveAndFlush(User.newActive(normalizedMobile));
            createdHere = true;
        } catch (DataIntegrityViolationException e) {
            user = userRepository.findLiveByMobile(normalizedMobile)
                    .orElseThrow(UserManagementErrors::authenticationFailed);
        }
        if (createdHere) {
            // Brand-new account: seed the default picsum avatar so a fresh user
            // always has a picture; they can replace it later via the avatar API.
            // REQUIRES_NEW inside the service isolates the external fetch from
            // this transaction; any failure is logged and swallowed there.
            try {
                userAvatarService.assignDefaultAvatar(user.getId());
            } catch (RuntimeException ignored) {
                // seeding must never break registration
            }
        }
        return user;
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
