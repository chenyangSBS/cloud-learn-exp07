package cs.sbs.web.dto;

public record AiTextGenerationResponse(
        String taskType,
        Long courseId,
        String courseTitle,
        String content) {
}
