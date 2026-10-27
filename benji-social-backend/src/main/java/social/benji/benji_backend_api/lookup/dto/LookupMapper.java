package social.benji.benji_backend_api.lookup.dto;

import org.springframework.stereotype.Component;
import social.benji.benji_backend_api.lookup.LookupModel;

@Component
public class LookupMapper {

    public LookupDto toDto(LookupModel model) {
        if (model == null) {
            return null;
        }
        return LookupDto.builder()
                .id(model.getId())
                .category(model.getCategory())
                .code(model.getCode())
                .title(model.getTitle())
                .description(model.getDescription())
                .active(model.getActive())
                .sortOrder(model.getSortOrder())
                .build();
    }

    public LookupModel toEntity(CreateLookupDto dto) {
        LookupModel model = new LookupModel();
        model.setCategory(normalize(dto.getCategory()));
        model.setCode(normalize(dto.getCode()));
        model.setTitle(dto.getTitle());
        model.setDescription(dto.getDescription());
        model.setActive(dto.getActive() == null ? Boolean.TRUE : dto.getActive());
        model.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        return model;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toLowerCase();
    }
}
