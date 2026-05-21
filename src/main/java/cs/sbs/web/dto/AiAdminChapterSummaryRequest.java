package cs.sbs.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record AiAdminChapterSummaryRequest(
        @NotNull(message = "课程ID不能为空")
        Long courseId,
        @NotBlank(message = "章节标题不能为空")
        String chapterTitle,
        @NotBlank(message = "章节内容不能为空")
        String chapterContent,
        Integer summaryLength,
        Map<String, Object> extraBody) {
}
