package cs.sbs.web.dto;

import java.math.BigDecimal;

public record CourseSqlViewResponse(
        Long courseId,
        String title,
        String teacher,
        String categoryName,
        Integer lessonCount,
        BigDecimal price) {
}
