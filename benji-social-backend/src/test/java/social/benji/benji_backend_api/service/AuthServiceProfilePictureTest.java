package social.benji.benji_backend_api.service;

import org.junit.jupiter.api.Test;
import social.benji.benji_backend_api.storage.MinioProperties;
import social.benji.benji_backend_api.storage.ObjectStorage;
import social.benji.benji_backend_api.usermanagement.domain.User;
import social.benji.benji_backend_api.usermanagement.repository.UserRepository;
import social.benji.benji_backend_api.usermanagement.service.DefaultUserAvatarService;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceProfilePictureTest {
    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final String BUCKET = "benji-bucket";

    @Test
    void databaseFailureDeletesNewObjectAndPreservesOldReplacement() {
        UserRepository users = mock(UserRepository.class);
        ObjectStorage storage = mock(ObjectStorage.class);
        User user = activeUser("old-key");
        when(users.findById(USER_ID)).thenReturn(Optional.of(user));
        doThrow(new RuntimeException("db failed")).when(users).saveAndFlush(any(User.class));
        DefaultUserAvatarService service = service(users, storage);

        assertThrows(RuntimeException.class,
                () -> service.upload(USER_ID, "image/png", png()));

        verify(storage).delete(eq(BUCKET), org.mockito.ArgumentMatchers.startsWith(
                "profile-pictures/" + USER_ID + "/"));
        verify(storage, never()).delete(BUCKET, "old-key");
    }

    @Test
    void successfulReplacementDeletesOldObjectAfterDatabaseSave() {
        UserRepository users = mock(UserRepository.class);
        ObjectStorage storage = mock(ObjectStorage.class);
        User user = activeUser("old-key");
        when(users.findById(USER_ID)).thenReturn(Optional.of(user));
        when(storage.presignedGetUrl(eq(BUCKET), any(), any(Duration.class))).thenReturn("url");
        DefaultUserAvatarService service = service(users, storage);

        service.upload(USER_ID, "image/jpeg", jpeg());

        verify(users).saveAndFlush(user);
        verify(storage).delete(BUCKET, "old-key");
    }

    @Test
    void oldObjectDeletionFailureDoesNotInvalidateSavedPicture() {
        UserRepository users = mock(UserRepository.class);
        ObjectStorage storage = mock(ObjectStorage.class);
        User user = activeUser("old-key");
        when(users.findById(USER_ID)).thenReturn(Optional.of(user));
        when(storage.presignedGetUrl(eq(BUCKET), any(), any(Duration.class))).thenReturn("url");
        doThrow(new RuntimeException("minio delete failed")).when(storage).delete(BUCKET, "old-key");
        DefaultUserAvatarService service = service(users, storage);

        service.upload(USER_ID, "image/jpeg", jpeg());

        verify(users).saveAndFlush(user);
        verify(storage).delete(BUCKET, "old-key");
    }

    private static DefaultUserAvatarService service(UserRepository users, ObjectStorage storage) {
        return new DefaultUserAvatarService(users, storage,
                new MinioProperties("http://localhost:9000", "minioadmin",
                        "minioadmin", BUCKET, 5));
    }

    private static User activeUser(String oldKey) {
        return User.builder()
                .id(USER_ID)
                .mobileNumber("+989120000000")
                .avatarObjectKey(oldKey)
                .build();
    }

    private static byte[] jpeg() {
        return new byte[]{(byte) 0xff, (byte) 0xd8, (byte) 0xff};
    }

    private static byte[] png() {
        return new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0d, 0x0a, 0x1a, 0x0a};
    }
}
