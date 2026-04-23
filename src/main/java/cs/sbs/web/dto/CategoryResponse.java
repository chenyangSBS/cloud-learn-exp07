package cs.sbs.web.dto;

import java.util.List;

public record CategoryResponse(
        Long id,
        String name,
        String description,
        Integer sortOrder,
        Long parentId,
        List<CategoryResponse> children) {
}
