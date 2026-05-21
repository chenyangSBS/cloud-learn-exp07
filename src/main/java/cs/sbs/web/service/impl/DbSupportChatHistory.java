package cs.sbs.web.service.impl;

import cs.sbs.web.entity.SupportChatMessage;
import cs.sbs.web.repository.SupportChatMessageRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class DbSupportChatHistory {

    private static final int MAX_MESSAGES_PER_CONVERSATION = 20;

    private final SupportChatMessageRepository supportChatMessageRepository;

    @Transactional(readOnly = true)
    public List<Message> getHistory(String conversationId) {
        if (!StringUtils.hasText(conversationId)) {
            return List.of();
        }
        List<SupportChatMessage> latest = supportChatMessageRepository.findByConversationId(
                        conversationId,
                        PageRequest.of(0, MAX_MESSAGES_PER_CONVERSATION, Sort.by(Sort.Direction.DESC, "id"))
                )
                .getContent();
        Collections.reverse(latest);

        List<Message> messages = new ArrayList<>(latest.size());
        for (SupportChatMessage msg : latest) {
            if (msg.getRole() == SupportChatMessage.Role.USER) {
                messages.add(new UserMessage(msg.getContent()));
            } else {
                messages.add(new AssistantMessage(msg.getContent()));
            }
        }
        return messages;
    }

    @Transactional
    public void appendUser(String conversationId, String userText) {
        append(conversationId, SupportChatMessage.Role.USER, userText);
    }

    @Transactional
    public void appendAssistant(String conversationId, String assistantText) {
        append(conversationId, SupportChatMessage.Role.ASSISTANT, assistantText);
    }

    @Transactional
    public void reset(String conversationId) {
        if (!StringUtils.hasText(conversationId)) {
            return;
        }
        supportChatMessageRepository.deleteByConversationId(conversationId);
    }

    private void append(String conversationId, SupportChatMessage.Role role, String content) {
        if (!StringUtils.hasText(conversationId)) {
            return;
        }
        SupportChatMessage message = new SupportChatMessage();
        message.setConversationId(conversationId);
        message.setRole(role);
        message.setContent(content);
        supportChatMessageRepository.save(message);
    }
}
