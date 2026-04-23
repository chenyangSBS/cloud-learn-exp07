package cs.sbs.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CourseResponse(
        Long id,
        String title,
        String teacher,
        String summary,
        BigDecimal price,
        Integer lessonCount,
        Boolean published,
        Long categoryId,
        String categoryName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
