package social.benji.benji_backend_api.petmedia.application;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import social.benji.benji_backend_api.pet.domain.Pet;
import social.benji.benji_backend_api.pet.service.PetService;
import social.benji.benji_backend_api.petmedia.domain.PetAlbum;
import social.benji.benji_backend_api.petmedia.domain.exception.MediaErrors;
import social.benji.benji_backend_api.petmedia.repository.PetAlbumRepository;

/**
 * Single choke point for the ownership chain User -> Pet -> Album -> Photo.
 * Every media operation passes through here, so object-level authorization is
 * never forgotten in a service method. Foreign ids always yield NOT_FOUND.
 */
@Component
public class OwnershipGuard {

    private final PetService pets;
    private final PetAlbumRepository albums;

    public OwnershipGuard(PetService pets, PetAlbumRepository albums) {
        this.pets = pets;
        this.albums = albums;
    }

    @Transactional(readOnly = true)
    public Pet requireOwnedPet(UUID petId, UUID ownerId) {
        return pets.requireOwnedPet(petId, ownerId);
    }

    /** Verifies pet ownership first, then that the album belongs to that pet. */
    @Transactional(readOnly = true)
    public PetAlbum requireOwnedAlbum(UUID petId, UUID albumId, UUID ownerId) {
        pets.requireOwnedPet(petId, ownerId);
        PetAlbum album = albums.findById(albumId).orElseThrow(MediaErrors::albumNotFound);
        if (!album.getPetId().equals(petId)) {
            throw MediaErrors.albumNotFound();
        }
        return album;
    }
}
