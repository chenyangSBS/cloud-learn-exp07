package cs.sbs.web.dto;

import java.time.LocalDateTime;

public record SupportChatMessageResponse(
        Long id,
        String role,
        String content,
        LocalDateTime createdAt) {
}

