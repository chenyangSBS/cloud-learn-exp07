package cs.sbs.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record AiAdminLessonObjectivesRequest(
        @NotNull(message = "课程ID不能为空")
        Long courseId,
        @NotBlank(message = "章节标题不能为空")
        String chapterTitle,
        @NotBlank(message = "课时标题不能为空")
        String lessonTitle,
        @NotBlank(message = "课时内容不能为空")
        String lessonContent,
        Integer objectiveCount,
        Map<String, Object> extraBody) {
}
