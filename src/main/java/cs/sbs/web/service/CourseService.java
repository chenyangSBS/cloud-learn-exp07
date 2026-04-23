package cs.sbs.web.service;

import cs.sbs.web.dto.CourseCreateRequest;
import cs.sbs.web.dto.CourseBatchPublishRequest;
import cs.sbs.web.dto.CoursePageResponse;
import cs.sbs.web.dto.CourseQueryRequest;
import cs.sbs.web.dto.CourseResponse;
import cs.sbs.web.dto.CourseSqlViewResponse;
import cs.sbs.web.dto.SqlUpdateResponse;
import java.math.BigDecimal;
import java.util.List;

public interface CourseService {

    CourseResponse create(CourseCreateRequest request);

    CoursePageResponse browsePublishedCatalog(Long categoryId, Integer page, Integer size, String sortBy, String direction);

    CoursePageResponse search(CourseQueryRequest request);

    List<CourseResponse> findByTeacherKeyword(String teacherKeyword);

    List<CourseResponse> batchPublish(CourseBatchPublishRequest request);

    List<CourseSqlViewResponse> getHotPublishedCoursesBySql(Integer limit);

    SqlUpdateResponse adjustCategoryPriceBySql(Long categoryId, BigDecimal delta);

    CourseResponse togglePublish(Long courseId, boolean published);
}
