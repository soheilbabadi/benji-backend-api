package social.benji.benji_backend_api.lostfound.domain;

import org.junit.jupiter.api.Test;
import social.benji.benji_backend_api.lostfound.application.ownership.OwnershipGuard;
import social.benji.benji_backend_api.lostfound.domain.model.FoundAdvertisement;
import social.benji.benji_backend_api.lostfound.domain.model.LostAdvertisement;
import social.benji.benji_backend_api.lostfound.domain.valueobject.Breed;
import social.benji.benji_backend_api.lostfound.domain.valueobject.Location;
import social.benji.benji_backend_api.lostfound.domain.valueobject.PetSpecies;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AdvertisementOwnershipTest {
    @Test
    void lostAndFoundAllowNullablePetId() {
        LostAdvertisement lost = new LostAdvertisement("owner-1", null,
                PetSpecies.DOG, Breed.unknown(), null, "Brown short-haired dog",
                null, LocalDate.now().minusDays(1),
                new Location("Iran", "Tehran", "Vanak", null), null);
        FoundAdvertisement found = new FoundAdvertisement("owner-1", null,
                PetSpecies.CAT, Breed.mixed(), "Small gray cat", null,
                LocalDate.now().minusDays(1),
                new Location("Iran", "Tehran", "Vanak", null), null);

        assertNull(lost.getPetId());
        assertNull(found.getPetId());
        assertTrue(lost.isOwnedBy("owner-1"));
        assertTrue(found.isOwnedBy("owner-1"));
    }

    @Test
    void rejectsCrossUserOwnership() {
        LostAdvertisement lost = new LostAdvertisement("owner-1", null,
                PetSpecies.DOG, Breed.unknown(), null, "Brown dog",
                null, LocalDate.now(),
                new Location("Iran", "Tehran", null, null), null);

        assertThrows(IllegalArgumentException.class,
                () -> OwnershipGuard.requireOwner(lost.getOwnerId(), "owner-2"));
    }
}
