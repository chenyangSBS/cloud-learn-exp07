package cs.sbs.web.repository;

import cs.sbs.web.entity.SupportChatMessage;
import cs.sbs.web.repository.projection.SupportConversationSummary;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SupportChatMessageRepository extends JpaRepository<SupportChatMessage, Long> {

    Page<SupportChatMessage> findByConversationId(String conversationId, Pageable pageable);

    List<SupportChatMessage> findByConversationIdOrderByIdAsc(String conversationId);

    @Query(
            value = """
                    select m.conversationId as conversationId,
                           max(m.createdAt) as lastMessageAt,
                           count(m.id) as messageCount
                    from SupportChatMessage m
                    group by m.conversationId
                    order by max(m.createdAt) desc
                    """,
            countQuery = """
                    select count(distinct m.conversationId)
                    from SupportChatMessage m
                    """
    )
    Page<SupportConversationSummary> findConversationSummaries(Pageable pageable);

    long deleteByConversationId(String conversationId);
}
