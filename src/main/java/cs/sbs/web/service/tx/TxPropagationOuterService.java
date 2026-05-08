package cs.sbs.web.service.tx;

import cs.sbs.web.entity.Category;
import cs.sbs.web.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TxPropagationOuterService {

    private final CategoryRepository categoryRepository;
    private final TxPropagationInnerService innerService;

    public TxPropagationOuterService(CategoryRepository categoryRepository, TxPropagationInnerService innerService) {
        this.categoryRepository = categoryRepository;
        this.innerService = innerService;
    }

    @Transactional
    public void requiredRollbackDemo(String outerCategoryName) {
        Category category = new Category();
        category.setName(outerCategoryName);
        category.setDescription("tx required outer insert");
        category.setSortOrder(800);
        categoryRepository.saveAndFlush(category);
        innerService.requiredFailRuntime();
    }

    @Transactional
    public void requiresNewDemo(String outerCategoryName, String innerCategoryName) {
        Category category = new Category();
        category.setName(outerCategoryName);
        category.setDescription("tx outer required insert");
        category.setSortOrder(810);
        categoryRepository.saveAndFlush(category);
        try {
            innerService.requiresNewInsertAndFail(innerCategoryName);
        } catch (RuntimeException ignored) {
        }
    }

    @Transactional
    public long notSupportedVisibilityDemo(String categoryName) {
        Category category = new Category();
        category.setName(categoryName);
        category.setDescription("tx not_supported visibility insert");
        category.setSortOrder(820);
        categoryRepository.saveAndFlush(category);
        return innerService.countByNameNotSupported(categoryName);
    }
}
