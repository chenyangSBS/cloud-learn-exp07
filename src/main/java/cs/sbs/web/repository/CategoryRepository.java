package cs.sbs.web.repository;

import cs.sbs.web.entity.Category;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface CategoryRepository extends CrudRepository<Category, Long> {

    List<Category> findByParentIsNullOrderBySortOrderAscIdAsc();

    List<Category> findAllByOrderBySortOrderAscIdAsc();

    boolean existsByParentId(Long parentId);

    boolean existsByName(String name);
}
