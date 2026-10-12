package social.benji.benji_backend_api.lostfound.service;

import java.util.UUID;

/**
 * Module boundary port: Lost & Found must not read the pet module's repositories directly.
 * Implemented by an adapter that delegates to PetRepository.findByIdAndOwnerId — ownership is
 * always evaluated server-side against the authenticated principal, never against client input.
 */
public interface PetOwnershipPort {

    /** True iff a non-archived pet with this id exists AND belongs to ownerId. */
    boolean ownsPet(UUID petId, UUID ownerId);
}
