package cs.sbs.web;

import static org.assertj.core.api.Assertions.assertThat;

import cs.sbs.web.dto.CourseSqlViewResponse;
import cs.sbs.web.entity.Category;
import cs.sbs.web.entity.Course;
import cs.sbs.web.repository.CategoryRepository;
import cs.sbs.web.repository.CourseCatalogRepository;
import cs.sbs.web.repository.CourseJdbcRepository;
import cs.sbs.web.repository.CourseRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class RepositoryAccessPatternTests {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CourseCatalogRepository courseCatalogRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseJdbcRepository courseJdbcRepository;

    @Test
    void crudRepositoryShouldSupportCategoryCrudFlow() {
        Category category = new Category();
        category.setName("测试分类-仓储模式");
        category.setDescription("用于 CrudRepository 示例");
        category.setSortOrder(99);

        Category saved = categoryRepository.save(category);

        assertThat(saved.getId()).isNotNull();
        assertThat(categoryRepository.existsByName("测试分类-仓储模式")).isTrue();
        assertThat(categoryRepository.findAllByOrderBySortOrderAscIdAsc()).isNotEmpty();

        categoryRepository.delete(saved);

        assertThat(categoryRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void pagingAndSortingRepositoryShouldBrowsePublishedCourses() {
        Page<Course> page = courseCatalogRepository.findByPublishedTrue(
                PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "lessonCount"))
        );

        assertThat(page.getContent()).hasSizeLessThanOrEqualTo(2);
        assertThat(page.getTotalElements()).isGreaterThan(0);
        assertThat(page.getContent()).allMatch(Course::getPublished);
    }

    @Test
    void jpaRepositoryShouldSupportDerivedQueryAndBatchFlush() {
        List<Course> courses = courseRepository.findByTeacherContainingIgnoreCaseOrderByIdDesc("老师");

        assertThat(courses).isNotEmpty();

        List<Course> selectedCourses = courses.stream().limit(2).toList();
        selectedCourses.forEach(course -> course.setPublished(Boolean.FALSE));
        List<Course> updatedCourses = courseRepository.saveAllAndFlush(selectedCourses);

        assertThat(updatedCourses).hasSize(2);
        assertThat(updatedCourses).allMatch(course -> !course.getPublished());
    }

    @Test
    void jdbcRepositoryShouldSupportSqlReadAndWrite() {
        List<CourseSqlViewResponse> rows = courseJdbcRepository.findHotPublishedCourses(3);

        assertThat(rows).hasSizeLessThanOrEqualTo(3);
        assertThat(rows).isNotEmpty();

        Long categoryId = courseRepository.findAll().getFirst().getCategory().getId();
        int affectedRows = courseJdbcRepository.increaseCategoryCoursePrice(categoryId, new BigDecimal("1.00"));

        assertThat(affectedRows).isGreaterThan(0);
        assertThat(courseJdbcRepository.countCoursesThatWillBecomeNegative(categoryId, new BigDecimal("-9999.00")))
                .isGreaterThan(0);
    }
}
