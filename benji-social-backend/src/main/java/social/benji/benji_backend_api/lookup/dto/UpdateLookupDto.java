package social.benji.benji_backend_api.lookup.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Partial (PATCH-style) update payload. Null fields are left unchanged.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLookupDto {

    @Size(max = 100)
    private String code;

    @Size(max = 255)
    private String title;

    @Size(max = 500)
    private String description;

    private Boolean active;

    private Integer sortOrder;
}
