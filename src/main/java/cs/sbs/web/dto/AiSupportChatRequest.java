package cs.sbs.web.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record AiSupportChatRequest(
        String conversationId,
        @NotBlank(message = "用户问题不能为空")
        String question,
        Boolean reset,
        Map<String, Object> extraBody) {
}
