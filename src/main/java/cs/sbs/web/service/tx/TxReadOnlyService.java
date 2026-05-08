package cs.sbs.web.service.tx;

import cs.sbs.web.entity.Category;
import cs.sbs.web.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TxReadOnlyService {

    private final CategoryRepository categoryRepository;

    public TxReadOnlyService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public void readOnlyAttemptWrite(String categoryName) {
        Category category = new Category();
        category.setName(categoryName);
        category.setDescription("tx readOnly attempt write");
        category.setSortOrder(860);
        categoryRepository.save(category);
    }
}
