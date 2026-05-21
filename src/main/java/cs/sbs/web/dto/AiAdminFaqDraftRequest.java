package cs.sbs.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record AiAdminFaqDraftRequest(
        @NotNull(message = "课程ID不能为空")
        Long courseId,
        @NotBlank(message = "FAQ 适用人群不能为空")
        String audience,
        @NotBlank(message = "FAQ 生成素材不能为空")
        String sourceMaterial,
        Integer faqCount,
        Map<String, Object> extraBody) {
}
