package social.benji.benji_backend_api.usermanagement.service;

import java.util.Optional;

import social.benji.benji_backend_api.usermanagement.domain.User;

public interface UserService {

    Optional<User> findByMobile(String normalizedMobile);

    User registerWithVerifiedMobile(String normalizedMobile);

    User findById(java.util.UUID id);

    User updateProfile(java.util.UUID userId, String firstName, String lastName, String email);

    void softDelete(java.util.UUID userId);
}
