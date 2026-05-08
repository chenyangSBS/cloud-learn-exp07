package cs.sbs.web.service.tx;

import cs.sbs.web.entity.Category;
import cs.sbs.web.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TxPropagationInnerService {

    private final CategoryRepository categoryRepository;

    public TxPropagationInnerService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void requiredFailRuntime() {
        throw new IllegalStateException("inner REQUIRED runtime exception");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void requiresNewInsertAndFail(String categoryName) {
        Category category = new Category();
        category.setName(categoryName);
        category.setDescription("tx requires_new insert");
        category.setSortOrder(900);
        categoryRepository.saveAndFlush(category);
        throw new IllegalStateException("inner REQUIRES_NEW runtime exception");
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED, readOnly = true)
    public long countByNameNotSupported(String categoryName) {
        return categoryRepository.countByName(categoryName);
    }
}
