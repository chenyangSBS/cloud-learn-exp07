package cs.sbs.web.repository.projection;

import java.time.LocalDateTime;

public interface SupportConversationSummary {

    String getConversationId();

    LocalDateTime getLastMessageAt();

    long getMessageCount();
}

