package cs.sbs.web.service.impl;

import cs.sbs.web.config.AiPromptProperties;
import cs.sbs.web.service.AiSupportService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class AiSupportServiceImpl implements AiSupportService {

    private final ChatClient chatClient;
    private final AiPromptProperties aiPromptProperties;
    private final DbSupportChatHistory chatHistory;

    @Override
    public Flux<String> streamReply(
            String conversationId,
            String question,
            boolean reset,
            Map<String, Object> extraBody
    ) {
        String safeConversationId = StringUtils.hasText(conversationId) ? conversationId.trim() : null;
        if (reset && safeConversationId != null) {
            chatHistory.reset(safeConversationId);
        }

        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(aiPromptProperties.getSupportSystemPrompt()));
        if (safeConversationId != null) {
            messages.addAll(chatHistory.getHistory(safeConversationId));
        }
        messages.add(new UserMessage(question));

        if (safeConversationId != null) {
            chatHistory.appendUser(safeConversationId, question);
        }

        StringBuilder assistantBuffer = new StringBuilder();
        OpenAiChatOptions options = buildOptions(extraBody);
        Prompt prompt = options == null ? new Prompt(messages) : new Prompt(messages, options);

        return chatClient.prompt(prompt)
                .stream()
                .content()
                .doOnNext(assistantBuffer::append)
                .doOnComplete(() -> {
                    if (safeConversationId != null) {
                        chatHistory.appendAssistant(safeConversationId, assistantBuffer.toString());
                    }
                });
    }

    private OpenAiChatOptions buildOptions(Map<String, Object> extraBody) {
        if (extraBody == null || extraBody.isEmpty()) {
            return null;
        }
        return OpenAiChatOptions.builder()
                .extraBody(extraBody)
                .build();
    }
}
