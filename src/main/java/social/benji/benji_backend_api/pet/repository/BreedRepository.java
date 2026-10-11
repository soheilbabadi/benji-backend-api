package social.benji.benji_backend_api.pet.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import social.benji.benji_backend_api.pet.domain.Breed;
import social.benji.benji_backend_api.pet.domain.PetSpecies;

public interface BreedRepository extends JpaRepository<Breed, UUID> {

    List<Breed> findBySpeciesOrderByNameAsc(PetSpecies species);
}
