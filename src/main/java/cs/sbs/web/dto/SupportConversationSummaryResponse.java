package cs.sbs.web.dto;

import java.time.LocalDateTime;

public record SupportConversationSummaryResponse(
        String conversationId,
        LocalDateTime lastMessageAt,
        long messageCount) {
}

