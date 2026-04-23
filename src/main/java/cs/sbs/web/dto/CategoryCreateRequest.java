package cs.sbs.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryCreateRequest(
        Long parentId,
        @NotBlank(message = "分类名称不能为空")
        @Size(max = 50, message = "分类名称长度不能超过50")
        String name,
        @Size(max = 200, message = "分类描述长度不能超过200")
        String description,
        Integer sortOrder) {
}
