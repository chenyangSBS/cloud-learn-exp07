package cs.sbs.web.service.tx;

import cs.sbs.web.entity.Category;
import cs.sbs.web.exception.TxCheckedException;
import cs.sbs.web.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TxRollbackService {

    private final CategoryRepository categoryRepository;

    public TxRollbackService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public void checkedExceptionDefaultCommit(String categoryName) throws TxCheckedException {
        Category category = new Category();
        category.setName(categoryName);
        category.setDescription("tx checked exception default");
        category.setSortOrder(830);
        categoryRepository.saveAndFlush(category);
        throw new TxCheckedException("checked exception default (should commit by default)");
    }

    @Transactional(rollbackFor = TxCheckedException.class)
    public void checkedExceptionRollbackFor(String categoryName) throws TxCheckedException {
        Category category = new Category();
        category.setName(categoryName);
        category.setDescription("tx checked exception rollbackFor");
        category.setSortOrder(840);
        categoryRepository.saveAndFlush(category);
        throw new TxCheckedException("checked exception rollbackFor (should rollback)");
    }

    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public void runtimeNoRollbackFor(String categoryName) {
        Category category = new Category();
        category.setName(categoryName);
        category.setDescription("tx runtime noRollbackFor");
        category.setSortOrder(850);
        categoryRepository.saveAndFlush(category);
        throw new IllegalArgumentException("runtime exception noRollbackFor (should commit)");
    }
}
