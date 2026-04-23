package cs.sbs.web.repository;

import cs.sbs.web.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface CourseCatalogRepository extends PagingAndSortingRepository<Course, Long> {

    Page<Course> findByPublishedTrue(Pageable pageable);

    Page<Course> findByCategoryIdAndPublishedTrue(Long categoryId, Pageable pageable);
}
