package cs.sbs.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record AiAdminCoursePolishRequest(
        @NotNull(message = "课程ID不能为空")
        Long courseId,
        @NotBlank(message = "原始课程简介不能为空")
        String draftIntroduction,
        String targetAudience,
        String tone,
        Map<String, Object> extraBody) {
}
