package cs.sbs.web.controller;

import cs.sbs.web.dto.ApiResponse;
import cs.sbs.web.dto.TxLabResult;
import cs.sbs.web.exception.TxCheckedException;
import cs.sbs.web.repository.CategoryRepository;
import cs.sbs.web.service.tx.TxIsolationService;
import cs.sbs.web.service.tx.TxPropagationOuterService;
import cs.sbs.web.service.tx.TxReadOnlyService;
import cs.sbs.web.service.tx.TxRollbackService;
import cs.sbs.web.service.tx.TxTimeoutService;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tx-lab")
public class TxLabController {

    private final CategoryRepository categoryRepository;
    private final TxPropagationOuterService propagationOuterService;
    private final TxRollbackService rollbackService;
    private final TxReadOnlyService readOnlyService;
    private final TxTimeoutService timeoutService;
    private final TxIsolationService isolationService;

    public TxLabController(
            CategoryRepository categoryRepository,
            TxPropagationOuterService propagationOuterService,
            TxRollbackService rollbackService,
            TxReadOnlyService readOnlyService,
            TxTimeoutService timeoutService,
            TxIsolationService isolationService) {
        this.categoryRepository = categoryRepository;
        this.propagationOuterService = propagationOuterService;
        this.rollbackService = rollbackService;
        this.readOnlyService = readOnlyService;
        this.timeoutService = timeoutService;
        this.isolationService = isolationService;
    }

    @GetMapping("/propagation/required")
    public ApiResponse<TxLabResult> propagationRequiredRollback() {
        String outerName = "tx-required-" + System.nanoTime();
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("outerCategoryName", outerName);
        try {
            propagationOuterService.requiredRollbackDemo(outerName);
            details.put("thrown", false);
        } catch (RuntimeException ex) {
            details.put("thrown", true);
            details.put("exception", ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
        details.put("existsAfterCall", categoryRepository.existsByName(outerName));
        return ApiResponse.ok("Propagation.REQUIRED 回滚演示完成", new TxLabResult("propagation_required_rollback", details));
    }

    @GetMapping("/propagation/requires-new")
    public ApiResponse<TxLabResult> propagationRequiresNewPartialCommit() {
        String outerName = "tx-outer-" + System.nanoTime();
        String innerName = "tx-inner-" + System.nanoTime();
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("outerCategoryName", outerName);
        details.put("innerCategoryName", innerName);
        propagationOuterService.requiresNewDemo(outerName, innerName);
        details.put("outerExistsAfterCall", categoryRepository.existsByName(outerName));
        details.put("innerExistsAfterCall", categoryRepository.existsByName(innerName));
        return ApiResponse.ok("Propagation.REQUIRES_NEW 局部提交演示完成", new TxLabResult("propagation_requires_new_partial_commit", details));
    }

    @GetMapping("/propagation/not-supported")
    public ApiResponse<TxLabResult> propagationNotSupportedVisibility() {
        String name = "tx-not-supported-" + System.nanoTime();
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("categoryName", name);
        long countDuring = propagationOuterService.notSupportedVisibilityDemo(name);
        details.put("countByNameDuringNotSupported", countDuring);
        details.put("existsAfterCall", categoryRepository.existsByName(name));
        return ApiResponse.ok("Propagation.NOT_SUPPORTED 可见性演示完成", new TxLabResult("propagation_not_supported_visibility", details));
    }

    @GetMapping("/rollback/checked-default")
    public ApiResponse<TxLabResult> rollbackCheckedDefault() {
        String name = "tx-checked-default-" + System.nanoTime();
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("categoryName", name);
        try {
            rollbackService.checkedExceptionDefaultCommit(name);
            details.put("thrown", false);
        } catch (TxCheckedException ex) {
            details.put("thrown", true);
            details.put("exception", ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
        details.put("existsAfterCall", categoryRepository.existsByName(name));
        return ApiResponse.ok("Checked Exception 默认提交演示完成", new TxLabResult("rollback_checked_default_commit", details));
    }

    @GetMapping("/rollback/checked-rollback-for")
    public ApiResponse<TxLabResult> rollbackCheckedRollbackFor() {
        String name = "tx-checked-rollback-" + System.nanoTime();
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("categoryName", name);
        try {
            rollbackService.checkedExceptionRollbackFor(name);
            details.put("thrown", false);
        } catch (TxCheckedException ex) {
            details.put("thrown", true);
            details.put("exception", ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
        details.put("existsAfterCall", categoryRepository.existsByName(name));
        return ApiResponse.ok("rollbackFor 指定回滚演示完成", new TxLabResult("rollback_for_checked_exception", details));
    }

    @GetMapping("/rollback/no-rollback-for")
    public ApiResponse<TxLabResult> rollbackNoRollbackFor() {
        String name = "tx-no-rollback-" + System.nanoTime();
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("categoryName", name);
        try {
            rollbackService.runtimeNoRollbackFor(name);
            details.put("thrown", false);
        } catch (RuntimeException ex) {
            details.put("thrown", true);
            details.put("exception", ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
        details.put("existsAfterCall", categoryRepository.existsByName(name));
        return ApiResponse.ok("noRollbackFor 指定不回滚演示完成", new TxLabResult("no_rollback_for_runtime_exception", details));
    }

    @GetMapping("/read-only")
    public ApiResponse<TxLabResult> readOnlyAttemptWrite() {
        String name = "tx-readonly-" + System.nanoTime();
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("categoryName", name);
        readOnlyService.readOnlyAttemptWrite(name);
        details.put("existsAfterCall", categoryRepository.existsByName(name));
        return ApiResponse.ok("readOnly 事务写入尝试演示完成", new TxLabResult("readonly_attempt_write", details));
    }

    @GetMapping("/timeout")
    public ApiResponse<TxLabResult> timeoutDemo() {
        String name = "tx-timeout-" + System.nanoTime();
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("categoryName", name);
        try {
            timeoutService.timeoutDemo(name);
            details.put("thrown", false);
        } catch (RuntimeException ex) {
            details.put("thrown", true);
            details.put("exception", ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
        details.put("existsAfterCall", categoryRepository.existsByName(name));
        return ApiResponse.ok("timeout 超时演示完成", new TxLabResult("timeout_demo", details));
    }

    @GetMapping("/isolation/read-committed")
    public ApiResponse<TxLabResult> isolationReadCommitted() {
        Map<String, Object> details = isolationService.nonRepeatableReadDemo(TransactionDefinition.ISOLATION_READ_COMMITTED);
        return ApiResponse.ok("Isolation.READ_COMMITTED 演示完成", new TxLabResult("isolation_read_committed", details));
    }

    @GetMapping("/isolation/repeatable-read")
    public ApiResponse<TxLabResult> isolationRepeatableRead() {
        Map<String, Object> details = isolationService.nonRepeatableReadDemo(TransactionDefinition.ISOLATION_REPEATABLE_READ);
        return ApiResponse.ok("Isolation.REPEATABLE_READ 演示完成", new TxLabResult("isolation_repeatable_read", details));
    }
}
