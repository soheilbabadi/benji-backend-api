package social.benji.benji_backend_api.lookup.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.benji.benji_backend_api.exception.BenjiCustomException;
import social.benji.benji_backend_api.lookup.LookupModel;
import social.benji.benji_backend_api.lookup.LookupRepository;
import social.benji.benji_backend_api.lookup.dto.CreateLookupDto;
import social.benji.benji_backend_api.lookup.dto.LookupDto;
import social.benji.benji_backend_api.lookup.dto.LookupMapper;
import social.benji.benji_backend_api.lookup.dto.UpdateLookupDto;
import social.benji.benji_backend_api.lookup.service.LookupService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LookupServiceImpl implements LookupService {

    private final LookupRepository lookupRepository;
    private final LookupMapper lookupMapper;

    @Override
    @Transactional(readOnly = true)
    public List<LookupDto> listActive() {
        return lookupRepository.findByActiveTrueOrderBySortOrderAsc().stream()
                .map(lookupMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LookupDto> listByCategory(String category) {
        requireText(category, "category is required");
        return lookupRepository
                .findByCategoryAndActiveTrueOrderBySortOrderAsc(normalize(category))
                .stream()
                .map(lookupMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LookupDto> listAllByCategory(String category) {
        requireText(category, "category is required");
        return lookupRepository.findByCategory(normalize(category)).stream()
                .map(lookupMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> listCategories() {
        return lookupRepository.findAll().stream()
                .map(LookupModel::getCategory)
                .distinct()
                .sorted()
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LookupDto findById(Long id) {
        return lookupMapper.toDto(getOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public LookupDto findByCategoryAndCode(String category, String code) {
        requireText(category, "category is required");
        requireText(code, "code is required");
        LookupModel model = lookupRepository
                .findByCategoryAndCode(normalize(category), normalize(code))
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException(
                        "Lookup not found for category '" + category + "' and code '" + code + "'"));
        return lookupMapper.toDto(model);
    }

    @Override
    @Transactional
    public LookupDto create(CreateLookupDto dto) {
        String category = normalize(dto.getCategory());
        String code = normalize(dto.getCode());
        lookupRepository.findByCategoryAndCode(category, code).ifPresent(existing -> {
            throw new BenjiCustomException.DuplicateEntityException(
                    "Lookup already exists: category='" + category + "', code='" + code + "'");
        });
        LookupModel saved = lookupRepository.save(lookupMapper.toEntity(dto));
        log.info("Created lookup id={} category={} code={}", saved.getId(), saved.getCategory(), saved.getCode());
        return lookupMapper.toDto(saved);
    }

    @Override
    @Transactional
    public LookupDto update(Long id, UpdateLookupDto dto) {
        LookupModel model = getOrThrow(id);

        if (dto.getCode() != null && !dto.getCode().isBlank()) {
            String newCode = normalize(dto.getCode());
            if (!newCode.equals(model.getCode())) {
                lookupRepository.findByCategoryAndCode(model.getCategory(), newCode)
                        .filter(other -> !other.getId().equals(id))
                        .ifPresent(other -> {
                            throw new BenjiCustomException.DuplicateEntityException(
                                    "Lookup code already exists in category '" + model.getCategory() + "': " + newCode);
                        });
                model.setCode(newCode);
            }
        }
        if (dto.getTitle() != null && !dto.getTitle().isBlank()) {
            model.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            model.setDescription(dto.getDescription());
        }
        if (dto.getActive() != null) {
            model.setActive(dto.getActive());
        }
        if (dto.getSortOrder() != null) {
            model.setSortOrder(dto.getSortOrder());
        }

        return lookupMapper.toDto(lookupRepository.save(model));
    }

    @Override
    @Transactional
    public LookupDto setActive(Long id, boolean active) {
        LookupModel model = getOrThrow(id);
        model.setActive(active);
        return lookupMapper.toDto(lookupRepository.save(model));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        LookupModel model = getOrThrow(id);
        lookupRepository.delete(model);
        log.info("Deleted lookup id={} category={} code={}", id, model.getCategory(), model.getCode());
    }

    private LookupModel getOrThrow(Long id) {
        if (id == null) {
            throw new BenjiCustomException.ValidationException("Lookup id is required");
        }
        return lookupRepository.findById(id)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("Lookup not found with id " + id));
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BenjiCustomException.ValidationException(message);
        }
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toLowerCase();
    }
}
