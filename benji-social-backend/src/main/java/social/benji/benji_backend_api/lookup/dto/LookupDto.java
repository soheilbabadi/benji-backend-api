package social.benji.benji_backend_api.lookup.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Read model returned by the lookup service.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LookupDto {

    private Long id;
    private String category;
    private String code;
    private String title;
    private String description;
    private Boolean active;
    private Integer sortOrder;
}
