package cs.sbs.web.dto;

import java.util.List;

public record SupportConversationPageResponse(
        long totalElements,
        int totalPages,
        int page,
        int size,
        List<SupportConversationSummaryResponse> items) {
}

