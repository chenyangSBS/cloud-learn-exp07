package cs.sbs.web.service.impl;

import cs.sbs.web.dto.CourseBatchPublishRequest;
import cs.sbs.web.dto.CourseCreateRequest;
import cs.sbs.web.dto.CoursePageResponse;
import cs.sbs.web.dto.CourseQueryRequest;
import cs.sbs.web.dto.CourseResponse;
import cs.sbs.web.dto.CourseSqlViewResponse;
import cs.sbs.web.dto.SqlUpdateResponse;
import cs.sbs.web.entity.Category;
import cs.sbs.web.entity.Course;
import cs.sbs.web.repository.CategoryRepository;
import cs.sbs.web.repository.CourseCatalogRepository;
import cs.sbs.web.repository.CourseJdbcRepository;
import cs.sbs.web.repository.CourseRepository;
import cs.sbs.web.service.CourseService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
@AllArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final CourseCatalogRepository courseCatalogRepository;
    private final CourseJdbcRepository courseJdbcRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public CourseResponse create(CourseCreateRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new EntityNotFoundException("课程分类不存在: " + request.categoryId()));

        Course course = new Course();
        course.setTitle(request.title());
        course.setTeacher(request.teacher());
        course.setSummary(request.summary());
        course.setPrice(request.price());
        course.setLessonCount(request.lessonCount());
        course.setPublished(Boolean.TRUE.equals(request.published()));
        course.setCategory(category);
        return toResponse(courseRepository.save(course));
    }

    @Override
    @Transactional(readOnly = true)
    public CoursePageResponse browsePublishedCatalog(Long categoryId, Integer page, Integer size, String sortBy, String direction) {
        Pageable pageable = PageRequest.of(
                safePage(page),
                safeSize(size),
                buildSort(sortBy, direction)
        );
        Page<Course> coursePage = categoryId == null
                ? courseCatalogRepository.findByPublishedTrue(pageable)
                : courseCatalogRepository.findByCategoryIdAndPublishedTrue(categoryId, pageable);
        return toPageResponse(coursePage);
    }

    @Override
    @Transactional(readOnly = true)
    public CoursePageResponse search(CourseQueryRequest request) {
        Pageable pageable = PageRequest.of(
                request.safePage(),
                request.safeSize(),
                Sort.by(Sort.Direction.DESC, "id")
        );
        Page<Course> page = courseRepository.findAll(buildSpecification(request), pageable);
        return toPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> findByTeacherKeyword(String teacherKeyword) {
        String safeKeyword = teacherKeyword == null ? "" : teacherKeyword.trim();
        return courseRepository.findByTeacherContainingIgnoreCaseOrderByIdDesc(safeKeyword)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<CourseResponse> batchPublish(CourseBatchPublishRequest request) {
        Set<Long> uniqueIds = new HashSet<>(request.courseIds());
        List<Course> courses = courseRepository.findAllById(uniqueIds);
        if (courses.size() != uniqueIds.size()) {
            throw new EntityNotFoundException("存在不存在的课程ID，批量更新失败");
        }
        for (Course course : courses) {
            course.setPublished(request.published());
        }
        return courseRepository.saveAllAndFlush(courses)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseSqlViewResponse> getHotPublishedCoursesBySql(Integer limit) {
        int safeLimit = limit == null || limit < 1 ? 5 : Math.min(limit, 20);
        return courseJdbcRepository.findHotPublishedCourses(safeLimit);
    }

    @Override
    public SqlUpdateResponse adjustCategoryPriceBySql(Long categoryId, BigDecimal delta) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("分类不存在: " + categoryId));
        if (delta == null) {
            throw new IllegalArgumentException("价格变动值不能为空");
        }
        int negativeCount = courseJdbcRepository.countCoursesThatWillBecomeNegative(categoryId, delta);
        if (negativeCount > 0) {
            throw new IllegalArgumentException("调价后会出现负数价格，已拒绝执行");
        }
        int affectedRows = courseJdbcRepository.increaseCategoryCoursePrice(categoryId, delta);
        return new SqlUpdateResponse(categoryId, delta, affectedRows);
    }

    @Override
    public CourseResponse togglePublish(Long courseId, boolean published) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("课程不存在: " + courseId));
        course.setPublished(published);
        return toResponse(courseRepository.save(course));
    }

    private Specification<Course> buildSpecification(CourseQueryRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(request.keyword())) {
                String likeValue = "%" + request.keyword().trim() + "%";
                predicates.add(cb.like(root.get("title"), likeValue));
            }
            if (request.published() != null) {
                predicates.add(cb.equal(root.get("published"), request.published()));
            }
            if (request.categoryId() != null) {
                Category category = categoryRepository.findById(request.categoryId())
                        .orElseThrow(() -> new EntityNotFoundException("分类不存在: " + request.categoryId()));
                Set<Long> categoryIds = new HashSet<>();
                collectCategoryIds(category, categoryIds);
                predicates.add(root.get("category").get("id").in(categoryIds));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void collectCategoryIds(Category category, Set<Long> categoryIds) {
        categoryIds.add(category.getId());
        for (Category child : category.getChildren()) {
            collectCategoryIds(child, categoryIds);
        }
    }

    private CoursePageResponse toPageResponse(Page<Course> page) {
        return new CoursePageResponse(
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize(),
                page.getContent().stream().map(this::toResponse).toList()
        );
    }

    private int safePage(Integer page) {
        return page == null || page < 0 ? 0 : page;
    }

    private int safeSize(Integer size) {
        if (size == null || size < 1) {
            return 5;
        }
        return Math.min(size, 20);
    }

    private Sort buildSort(String sortBy, String direction) {
        String safeSortBy = switch (sortBy == null ? "id" : sortBy) {
            case "price" -> "price";
            case "lessonCount" -> "lessonCount";
            case "title" -> "title";
            default -> "id";
        };
        Sort.Direction safeDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(safeDirection, safeSortBy);
    }

    private CourseResponse toResponse(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getTitle(),
                course.getTeacher(),
                course.getSummary(),
                course.getPrice(),
                course.getLessonCount(),
                course.getPublished(),
                course.getCategory().getId(),
                course.getCategory().getName(),
                course.getCreatedAt(),
                course.getUpdatedAt()
        );
    }
}
