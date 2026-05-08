package cs.sbs.web.controller;

import cs.sbs.web.dto.ApiResponse;
import cs.sbs.web.dto.CourseBatchPublishRequest;
import cs.sbs.web.dto.CourseCreateRequest;
import cs.sbs.web.dto.CoursePageResponse;
import cs.sbs.web.dto.CourseQueryRequest;
import cs.sbs.web.dto.CourseQbeQueryRequest;
import cs.sbs.web.dto.CourseResponse;
import cs.sbs.web.dto.CourseSpecQueryRequest;
import cs.sbs.web.dto.CourseSqlViewResponse;
import cs.sbs.web.dto.SqlUpdateResponse;
import cs.sbs.web.service.CourseService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping
    public ApiResponse<CourseResponse> create(@Valid @RequestBody CourseCreateRequest request) {
        return ApiResponse.ok("课程创建成功", courseService.create(request));
    }

    @GetMapping("/catalog")
    public ApiResponse<CoursePageResponse> browseCatalog(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String direction) {
        return ApiResponse.ok(
                "课程目录查询成功",
                courseService.browsePublishedCatalog(categoryId, page, size, sortBy, direction)
        );
    }

    @GetMapping
    public ApiResponse<CoursePageResponse> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean published,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        CourseQueryRequest queryRequest = new CourseQueryRequest(keyword, categoryId, published, page, size);
        return ApiResponse.ok("课程分页查询成功", courseService.search(queryRequest));
    }

    @GetMapping("/qbe")
    public ApiResponse<CoursePageResponse> qbeSearch(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String teacher,
            @RequestParam(required = false) Boolean published,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        CourseQbeQueryRequest request = new CourseQbeQueryRequest(title, teacher, published, page, size);
        return ApiResponse.ok("课程 QBE 查询成功", courseService.qbeSearch(request));
    }

    @GetMapping("/spec")
    public ApiResponse<CoursePageResponse> specSearch(
            @RequestParam(required = false) String titleKeyword,
            @RequestParam(required = false) String teacherKeyword,
            @RequestParam(required = false) Boolean published,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer minLessonCount,
            @RequestParam(required = false) Integer maxLessonCount,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String direction) {
        CourseSpecQueryRequest request = new CourseSpecQueryRequest(
                titleKeyword,
                teacherKeyword,
                published,
                categoryId,
                minPrice,
                maxPrice,
                minLessonCount,
                maxLessonCount,
                page,
                size,
                sortBy,
                direction
        );
        return ApiResponse.ok("课程 Specification 查询成功", courseService.specSearch(request));
    }

    @GetMapping("/teachers")
    public ApiResponse<List<CourseResponse>> findByTeacher(@RequestParam(required = false) String teacherKeyword) {
        return ApiResponse.ok("教师课程查询成功", courseService.findByTeacherKeyword(teacherKeyword));
    }

    @PatchMapping("/batch-publish")
    public ApiResponse<List<CourseResponse>> batchPublish(@Valid @RequestBody CourseBatchPublishRequest request) {
        return ApiResponse.ok("课程批量上下架成功", courseService.batchPublish(request));
    }

    @PatchMapping("/{courseId}/publish")
    public ApiResponse<CourseResponse> publish(@PathVariable Long courseId, @RequestParam boolean published) {
        return ApiResponse.ok("课程状态更新成功", courseService.togglePublish(courseId, published));
    }

    @GetMapping("/sql/hot")
    public ApiResponse<List<CourseSqlViewResponse>> hotCoursesBySql(@RequestParam(required = false) Integer limit) {
        return ApiResponse.ok("SQL 榜单查询成功", courseService.getHotPublishedCoursesBySql(limit));
    }

    @PatchMapping("/sql/category-price")
    public ApiResponse<SqlUpdateResponse> adjustCategoryPrice(
            @RequestParam Long categoryId,
            @RequestParam BigDecimal delta) {
        return ApiResponse.ok("SQL 调价执行成功", courseService.adjustCategoryPriceBySql(categoryId, delta));
    }
}
