package social.benji.benji_backend_api.service;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import social.benji.benji_backend_api.dto.AuthResponse;
import social.benji.benji_backend_api.model.User;
import social.benji.benji_backend_api.repository.RoleRepository;
import social.benji.benji_backend_api.repository.UserRepository;
import social.benji.benji_backend_api.service.AuthService;
import social.benji.benji_backend_api.service.EmailService;
import social.benji.benji_backend_api.service.ObjectStorageService;
import social.benji.benji_backend_api.service.UploadedObjectMetadata;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceProfilePictureTest {
    @Test
    void databaseFailureDeletesNewObjectAndPreservesNoReplacement() {
        UserRepository users = mock(UserRepository.class);
        RoleRepository roles = mock(RoleRepository.class);
        EmailService email = mock(EmailService.class);
        ObjectStorageService storage = mock(ObjectStorageService.class);

        User user = User.builder().id("11111111-1111-1111-1111-111111111111")
                .email("owner@example.com").profilePictureFileId("old-key").build();
        when(users.findByEmail("owner@example.com")).thenReturn(java.util.Optional.of(user));
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png",
                "image/png", new byte[]{(byte)0x89, 'P', 'N', 'G'});
        when(storage.upload(file, "profile-pictures"))
                .thenReturn(new UploadedObjectMetadata("new-key", "avatar.png", "image/png", 4));
        doThrow(new RuntimeException("db failed")).when(users).saveAndFlush(any(User.class));

        AuthService service = new AuthService(users, roles, email, storage);

        assertThrows(RuntimeException.class,
                () -> service.uploadProfilePicture("owner@example.com", file));
        verify(storage).delete("new-key");
        verify(storage, never()).delete("old-key");
    }

    @Test
    void successfulReplacementDeletesOldObjectOnlyAfterDatabaseSave() {
        UserRepository users = mock(UserRepository.class);
        RoleRepository roles = mock(RoleRepository.class);
        EmailService email = mock(EmailService.class);
        ObjectStorageService storage = mock(ObjectStorageService.class);

        User user = User.builder().id("11111111-1111-1111-1111-111111111111")
                .email("owner@example.com").profilePictureFileId("old-key").build();
        when(users.findByEmail("owner@example.com")).thenReturn(java.util.Optional.of(user));
        MockMultipartFile file = new MockMultipartFile("file", "avatar.jpg",
                "image/jpeg", new byte[]{(byte)0xff, (byte)0xd8, (byte)0xff});
        when(storage.upload(file, "profile-pictures"))
                .thenReturn(new UploadedObjectMetadata("new-key", "avatar.jpg", "image/jpeg", 3));
        when(users.saveAndFlush(any(User.class))).thenReturn(user);

        AuthService service = new AuthService(users, roles, email, storage);
        AuthResponse response = service.uploadProfilePicture("owner@example.com", file);

        assertEquals("new-key", response.getProfilePictureFileId());
        verify(users).saveAndFlush(user);
        verify(storage).delete("old-key");
    }

    @Test
    void oldObjectDeletionFailureDoesNotInvalidateSavedPicture() {
        UserRepository users = mock(UserRepository.class);
        RoleRepository roles = mock(RoleRepository.class);
        EmailService email = mock(EmailService.class);
        ObjectStorageService storage = mock(ObjectStorageService.class);

        User user = User.builder().id("11111111-1111-1111-1111-111111111111")
                .email("owner@example.com").profilePictureFileId("old-key").build();
        when(users.findByEmail("owner@example.com")).thenReturn(java.util.Optional.of(user));
        MockMultipartFile file = new MockMultipartFile("file", "avatar.jpg",
                "image/jpeg", new byte[]{(byte)0xff, (byte)0xd8, (byte)0xff});
        when(storage.upload(file, "profile-pictures"))
                .thenReturn(new UploadedObjectMetadata("new-key", "avatar.jpg", "image/jpeg", 3));
        when(users.saveAndFlush(any(User.class))).thenReturn(user);
        doThrow(new RuntimeException("minio delete failed")).when(storage).delete("old-key");

        AuthService service = new AuthService(users, roles, email, storage);
        AuthResponse response = service.uploadProfilePicture("owner@example.com", file);

        assertEquals("new-key", response.getProfilePictureFileId());
    }
}
