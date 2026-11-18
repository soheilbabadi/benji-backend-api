package social.benji.benji_backend_api.lostfound.application.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CreateLostAdvertisementRequest(
        UUID petId,
        String species,
        String breed,
        String petName,
        String physicalDescription,
        String distinguishingCharacteristics,
        LocalDate lostDate,
        String country,
        String city,
        String area,
        String locationDescription,
        String contactInformation
) {}
