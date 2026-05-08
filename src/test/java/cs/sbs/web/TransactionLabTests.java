package cs.sbs.web;

import static org.assertj.core.api.Assertions.assertThat;

import cs.sbs.web.entity.Category;
import cs.sbs.web.exception.TxCheckedException;
import cs.sbs.web.repository.CategoryRepository;
import cs.sbs.web.service.tx.TxPropagationOuterService;
import cs.sbs.web.service.tx.TxRollbackService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TransactionLabTests {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TxPropagationOuterService propagationOuterService;

    @Autowired
    private TxRollbackService rollbackService;

    @Test
    void requiredShouldRollbackAll() {
        String name = "test-tx-required-" + System.nanoTime();
        try {
            propagationOuterService.requiredRollbackDemo(name);
        } catch (RuntimeException ignored) {
        }
        assertThat(categoryRepository.existsByName(name)).isFalse();
    }

    @Test
    void requiresNewShouldRollbackInnerButCommitOuter() {
        String outerName = "test-tx-outer-" + System.nanoTime();
        String innerName = "test-tx-inner-" + System.nanoTime();
        propagationOuterService.requiresNewDemo(outerName, innerName);
        assertThat(categoryRepository.existsByName(outerName)).isTrue();
        assertThat(categoryRepository.existsByName(innerName)).isFalse();
        Category outer = categoryRepository.findByName(outerName).orElseThrow();
        categoryRepository.delete(outer);
    }

    @Test
    void notSupportedShouldNotSeeUncommittedWrite() {
        String name = "test-tx-not-supported-" + System.nanoTime();
        long countDuring = propagationOuterService.notSupportedVisibilityDemo(name);
        assertThat(countDuring).isEqualTo(0);
        assertThat(categoryRepository.existsByName(name)).isTrue();
        Category saved = categoryRepository.findByName(name).orElseThrow();
        categoryRepository.delete(saved);
    }

    @Test
    void checkedExceptionShouldCommitByDefault() {
        String name = "test-tx-checked-default-" + System.nanoTime();
        try {
            rollbackService.checkedExceptionDefaultCommit(name);
        } catch (TxCheckedException ignored) {
        }
        assertThat(categoryRepository.existsByName(name)).isTrue();
        Category saved = categoryRepository.findByName(name).orElseThrow();
        categoryRepository.delete(saved);
    }

    @Test
    void rollbackForShouldRollbackCheckedException() {
        String name = "test-tx-checked-rollback-" + System.nanoTime();
        try {
            rollbackService.checkedExceptionRollbackFor(name);
        } catch (TxCheckedException ignored) {
        }
        assertThat(categoryRepository.existsByName(name)).isFalse();
    }

    @Test
    void noRollbackForShouldCommitEvenWithRuntimeException() {
        String name = "test-tx-no-rollback-" + System.nanoTime();
        try {
            rollbackService.runtimeNoRollbackFor(name);
        } catch (RuntimeException ignored) {
        }
        assertThat(categoryRepository.existsByName(name)).isTrue();
        Category saved = categoryRepository.findByName(name).orElseThrow();
        categoryRepository.delete(saved);
    }
}
