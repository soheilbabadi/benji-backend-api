package social.benji.benji_backend_api.lostfound.application;

import org.junit.jupiter.api.Test;
import social.benji.benji_backend_api.lostfound.application.dto.CreateFoundAdvertisementRequest;
import social.benji.benji_backend_api.lostfound.application.dto.CreateLostAdvertisementRequest;
import social.benji.benji_backend_api.lostfound.application.ownership.AuthenticatedUserIdResolver;
import social.benji.benji_backend_api.lostfound.application.pet.PetOwnershipValidator;
import social.benji.benji_backend_api.lostfound.domain.model.FoundAdvertisement;
import social.benji.benji_backend_api.lostfound.domain.model.LostAdvertisement;
import social.benji.benji_backend_api.lostfound.domain.repository.FoundAdvertisementRepository;
import social.benji.benji_backend_api.lostfound.domain.repository.LostAdvertisementRepository;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LostFoundApplicationServiceTest {
    @Test
    void lostCreationAssignsAuthenticatedOwnerWithoutClientOwnerId() {
        LostAdvertisementRepository repository = mock(LostAdvertisementRepository.class);
        AuthenticatedUserIdResolver resolver = mock(AuthenticatedUserIdResolver.class);
        PetOwnershipValidator petValidator = mock(PetOwnershipValidator.class);
        when(resolver.currentUserId()).thenReturn("owner-1");
        when(repository.save(any(LostAdvertisement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LostAdvertisementApplicationService service =
                new LostAdvertisementApplicationService(repository, resolver, petValidator);
        LostAdvertisement result = service.create(validLostRequest(null));

        assertEquals("owner-1", result.getOwnerId());
        verify(repository).save(any(LostAdvertisement.class));
    }

    @Test
    void foundCreationIsIndependentAndSupportsNullPetId() {
        FoundAdvertisementRepository repository = mock(FoundAdvertisementRepository.class);
        AuthenticatedUserIdResolver resolver = mock(AuthenticatedUserIdResolver.class);
        PetOwnershipValidator petValidator = mock(PetOwnershipValidator.class);
        when(resolver.currentUserId()).thenReturn("owner-2");
        when(repository.save(any(FoundAdvertisement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        FoundAdvertisementApplicationService service =
                new FoundAdvertisementApplicationService(repository, resolver, petValidator);
        FoundAdvertisement result = service.create(validFoundRequest(null));

        assertEquals("owner-2", result.getOwnerId());
        assertNull(result.getPetId());
        verify(petValidator, never()).validatePetOwnership(any(), any());
        verify(repository).save(any(FoundAdvertisement.class));
    }

    @Test
    void nonNullPetIdInvokesValidatorAndInvalidOwnershipPreventsSave() {
        UUID petId = UUID.randomUUID();
        LostAdvertisementRepository repository = mock(LostAdvertisementRepository.class);
        AuthenticatedUserIdResolver resolver = mock(AuthenticatedUserIdResolver.class);
        PetOwnershipValidator petValidator = mock(PetOwnershipValidator.class);
        when(resolver.currentUserId()).thenReturn("owner-1");
        when(petValidator.validatePetOwnership(petId, "owner-1")).thenReturn(false);

        LostAdvertisementApplicationService service =
                new LostAdvertisementApplicationService(repository, resolver, petValidator);

        assertThrows(IllegalArgumentException.class,
                () -> service.create(validLostRequest(petId)));
        verify(petValidator).validatePetOwnership(petId, "owner-1");
        verify(repository, never()).save(any());
    }

    @Test
    void missingAuthenticationPreventsRepositorySave() {
        LostAdvertisementRepository repository = mock(LostAdvertisementRepository.class);
        AuthenticatedUserIdResolver resolver = mock(AuthenticatedUserIdResolver.class);
        PetOwnershipValidator petValidator = mock(PetOwnershipValidator.class);
        when(resolver.currentUserId()).thenThrow(
                new IllegalStateException("Authentication is required"));

        LostAdvertisementApplicationService service =
                new LostAdvertisementApplicationService(repository, resolver, petValidator);

        assertThrows(IllegalStateException.class,
                () -> service.create(validLostRequest(null)));
        verify(repository, never()).save(any());
    }

    @Test
    void nullRequestIsRejectedBeforeOwnerResolutionOrSave() {
        LostAdvertisementRepository repository = mock(LostAdvertisementRepository.class);
        AuthenticatedUserIdResolver resolver = mock(AuthenticatedUserIdResolver.class);
        PetOwnershipValidator petValidator = mock(PetOwnershipValidator.class);
        LostAdvertisementApplicationService service =
                new LostAdvertisementApplicationService(repository, resolver, petValidator);

        assertThrows(IllegalArgumentException.class, () -> service.create(null));
        verifyNoInteractions(resolver, petValidator, repository);
    }

    private CreateLostAdvertisementRequest validLostRequest(UUID petId) {
        return new CreateLostAdvertisementRequest(
                petId, "DOG", null, null, "Brown dog",
                null, LocalDate.now().minusDays(1),
                "Iran", "Tehran", null, null, null);
    }

    private CreateFoundAdvertisementRequest validFoundRequest(UUID petId) {
        return new CreateFoundAdvertisementRequest(
                petId, "CAT", null, "Gray cat",
                null, LocalDate.now().minusDays(1),
                "Iran", "Tehran", null, null, null);
    }
}
