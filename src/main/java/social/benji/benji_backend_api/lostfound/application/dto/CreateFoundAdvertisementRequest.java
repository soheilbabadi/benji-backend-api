package social.benji.benji_backend_api.lostfound.application.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CreateFoundAdvertisementRequest(
        UUID petId,
        String species,
        String breed,
        String physicalDescription,
        String distinguishingCharacteristics,
        LocalDate foundDate,
        String country,
        String city,
        String area,
        String locationDescription,
        String contactInformation
) {}
