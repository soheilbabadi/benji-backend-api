package social.benji.benji_backend_api.lookup.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Payload for creating a new lookup entry.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLookupDto {

    @NotBlank(message = "category is required")
    @Size(max = 100)
    private String category;

    @NotBlank(message = "code is required")
    @Size(max = 100)
    private String code;

    @NotBlank(message = "title is required")
    @Size(max = 255)
    private String title;

    @Size(max = 500)
    private String description;

    /** Defaults to true when omitted. */
    private Boolean active;

    /** Defaults to 0 when omitted. */
    private Integer sortOrder;
}
