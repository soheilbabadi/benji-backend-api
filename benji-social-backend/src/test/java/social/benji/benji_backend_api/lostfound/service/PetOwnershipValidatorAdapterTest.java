package social.benji.benji_backend_api.lostfound.service;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PetOwnershipValidatorAdapterTest {
    @Test
    void nullPetIdIsAcceptedWithoutOwnershipLookup() {
        PetOwnershipPort port = mock(PetOwnershipPort.class);
        PetOwnershipValidatorAdapter adapter = new PetOwnershipValidatorAdapter(port);

        assertTrue(adapter.validatePetOwnership(null, "owner-1"));
        verifyNoInteractions(port);
    }

    @Test
    void nonNullPetIdUsesTheRealPortAdapter() {
        PetOwnershipPort port = mock(PetOwnershipPort.class);
        UUID petId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        when(port.ownsPet(petId, ownerId)).thenReturn(true);
        PetOwnershipValidatorAdapter adapter = new PetOwnershipValidatorAdapter(port);

        assertTrue(adapter.validatePetOwnership(petId, ownerId.toString()));
        verify(port).ownsPet(petId, ownerId);
    }

    @Test
    void malformedOwnerIdFailsSafely() {
        PetOwnershipPort port = mock(PetOwnershipPort.class);
        PetOwnershipValidatorAdapter adapter = new PetOwnershipValidatorAdapter(port);

        assertThrows(IllegalStateException.class,
                () -> adapter.validatePetOwnership(UUID.randomUUID(), "not-a-uuid"));
        verifyNoInteractions(port);
    }
}
