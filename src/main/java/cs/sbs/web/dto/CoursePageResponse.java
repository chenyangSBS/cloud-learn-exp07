package cs.sbs.web.dto;

import java.util.List;

public record CoursePageResponse(
        long totalElements,
        int totalPages,
        int pageNumber,
        int pageSize,
        List<CourseResponse> records) {
}
