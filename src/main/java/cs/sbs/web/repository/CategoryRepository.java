package cs.sbs.web.repository;

import cs.sbs.web.entity.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByParentIsNullOrderBySortOrderAscIdAsc();

    List<Category> findAllByOrderBySortOrderAscIdAsc();

    boolean existsByParentId(Long parentId);

    boolean existsByName(String name);

    long countByName(String name);

    Optional<Category> findByName(String name);
}
