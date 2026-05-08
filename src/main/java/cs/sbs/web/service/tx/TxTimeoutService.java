package cs.sbs.web.service.tx;

import cs.sbs.web.entity.Category;
import cs.sbs.web.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TxTimeoutService {

    private final CategoryRepository categoryRepository;

    public TxTimeoutService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(timeout = 1)
    public void timeoutDemo(String categoryName) {
        Category category = new Category();
        category.setName(categoryName);
        category.setDescription("tx timeout demo");
        category.setSortOrder(870);
        categoryRepository.saveAndFlush(category);
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        category.setDescription("tx timeout demo after sleep");
        categoryRepository.saveAndFlush(category);
    }
}
