package social.benji.benji_backend_api.consultation.dto;

import lombok.*;
import social.benji.benji_backend_api.consultation.domain.valueobject.PetDataType;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationSharedPetDataDto {

    private String id;
    private String consultationId;
    private PetDataType dataType;
    private String contentSnapshot;
    private Instant createdAt;
}
