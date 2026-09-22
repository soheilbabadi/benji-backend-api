package social.benji.benji_backend_api.lookup;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LookupRepository extends JpaRepository<LookupModel, Long> {

    Optional<LookupModel> findByCategoryAndCode(String category, String code);

    List<LookupModel> findByCategory(String category);

    List<LookupModel> findByActiveTrueOrderBySortOrderAsc();

    List<LookupModel> findByCategoryAndActiveTrueOrderBySortOrderAsc(String category);
}
