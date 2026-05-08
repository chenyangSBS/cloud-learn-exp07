package cs.sbs.web.service.tx;

import cs.sbs.web.entity.Course;
import cs.sbs.web.repository.CourseRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.stereotype.Service;

@Service
public class TxIsolationService {

    private final PlatformTransactionManager transactionManager;
    private final CourseRepository courseRepository;

    public TxIsolationService(PlatformTransactionManager transactionManager, CourseRepository courseRepository) {
        this.transactionManager = transactionManager;
        this.courseRepository = courseRepository;
    }

    public Map<String, Object> nonRepeatableReadDemo(int isolationLevel) {
        Long courseId = courseRepository.findAll(PageRequest.of(0, 1)).getContent().getFirst().getId();

        TransactionTemplate templateA = new TransactionTemplate(transactionManager);
        templateA.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        templateA.setIsolationLevel(isolationLevel);

        TransactionTemplate templateB = new TransactionTemplate(transactionManager);
        templateB.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        templateB.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);

        CountDownLatch aFirstReadDone = new CountDownLatch(1);
        CountDownLatch bCommitDone = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            String originalTitle = courseRepository.findById(courseId).orElseThrow().getTitle();
            String updateTitle = originalTitle + " (tx-update)";

            Future<Map<String, String>> aResult = executor.submit(() -> templateA.execute(status -> {
                Map<String, String> reads = new LinkedHashMap<>();
                String first = courseRepository.findById(courseId).orElseThrow().getTitle();
                reads.put("firstRead", first);
                aFirstReadDone.countDown();
                try {
                    bCommitDone.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                String second = courseRepository.findById(courseId).orElseThrow().getTitle();
                reads.put("secondRead", second);
                return reads;
            }));

            Future<Void> bResult = executor.submit(() -> {
                try {
                    aFirstReadDone.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                templateB.execute(status -> {
                    Course course = courseRepository.findById(courseId).orElseThrow();
                    course.setTitle(updateTitle);
                    courseRepository.saveAndFlush(course);
                    return null;
                });
                bCommitDone.countDown();
                return null;
            });

            bResult.get(8, TimeUnit.SECONDS);
            Map<String, String> reads = aResult.get(8, TimeUnit.SECONDS);

            templateB.execute(status -> {
                Course course = courseRepository.findById(courseId).orElseThrow();
                course.setTitle(originalTitle);
                courseRepository.saveAndFlush(course);
                return null;
            });

            Map<String, Object> details = new LinkedHashMap<>();
            details.put("courseId", courseId);
            details.put("originalTitle", originalTitle);
            details.put("updatedTitle", updateTitle);
            details.putAll(reads);
            return details;
        } catch (Exception e) {
            Map<String, Object> details = new LinkedHashMap<>();
            details.put("error", e.getClass().getSimpleName() + ": " + e.getMessage());
            return details;
        } finally {
            executor.shutdownNow();
        }
    }
}
