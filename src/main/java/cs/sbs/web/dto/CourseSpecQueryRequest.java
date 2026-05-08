package cs.sbs.web.dto;

import java.math.BigDecimal;

public record CourseSpecQueryRequest(
        String titleKeyword,
        String teacherKeyword,
        Boolean published,
        Long categoryId,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Integer minLessonCount,
        Integer maxLessonCount,
        Integer page,
        Integer size,
        String sortBy,
        String direction) {

    public int safePage() {
        return page == null || page < 0 ? 0 : page;
    }

    public int safeSize() {
        if (size == null || size < 1) {
            return 5;
        }
        return Math.min(size, 20);
    }
}
