package social.benji.benji_backend_api.consultation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import social.benji.benji_backend_api.consultation.domain.valueobject.PetDataType;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationSharedPetDataDto {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String id;
    private String consultationId;
    private PetDataType dataType;
    private String contentSnapshot;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Instant createdAt;
}
