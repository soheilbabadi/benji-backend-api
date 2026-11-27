package social.benji.benji_backend_api.lookup.service;

import social.benji.benji_backend_api.lookup.dto.CreateLookupDto;
import social.benji.benji_backend_api.lookup.dto.LookupDto;
import social.benji.benji_backend_api.lookup.dto.UpdateLookupDto;

import java.util.List;

/**
 * Service layer for lookup/reference data (categories, pet species, blog tags, etc.).
 */
public interface LookupService {

    /** All active lookups ordered by sort order. */
    List<LookupDto> listActive();

    /** Active lookups for a category, ordered by sort order. */
    List<LookupDto> listByCategory(String category);

    /** All lookups (including inactive) for a category — admin view. */
    List<LookupDto> listAllByCategory(String category);

    /** Distinct categories present in the table. */
    List<String> listCategories();

    LookupDto findById(Long id);

    LookupDto findByCategoryAndCode(String category, String code);

    LookupDto create(CreateLookupDto dto);

    /** Partial update: null fields are left unchanged. */
    LookupDto update(Long id, UpdateLookupDto dto);

    /** Convenience toggle used by admin UIs. */
    LookupDto setActive(Long id, boolean active);

    void deleteById(Long id);
}
