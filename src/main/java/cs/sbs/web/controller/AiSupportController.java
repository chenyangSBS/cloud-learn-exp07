package cs.sbs.web.controller;

import cs.sbs.web.dto.AiSupportChatRequest;
import cs.sbs.web.dto.ApiResponse;
import cs.sbs.web.dto.SupportChatMessageResponse;
import cs.sbs.web.dto.SupportConversationPageResponse;
import cs.sbs.web.dto.SupportConversationSummaryResponse;
import cs.sbs.web.entity.SupportChatMessage;
import cs.sbs.web.repository.SupportChatMessageRepository;
import cs.sbs.web.repository.projection.SupportConversationSummary;
import cs.sbs.web.service.AiSupportService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/ai/support")
@RequiredArgsConstructor
public class AiSupportController {

    private final AiSupportService aiSupportService;
    private final SupportChatMessageRepository supportChatMessageRepository;

    @PostMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamReply(@Valid @RequestBody AiSupportChatRequest request) {
        SseEmitter emitter = new SseEmitter(0L);
        aiSupportService.streamReply(
                        request.conversationId(),
                        request.question(),
                        Boolean.TRUE.equals(request.reset()),
                        request.extraBody()
                )
                .doOnNext(chunk -> sendEvent(emitter, "message", chunk))
                .doOnComplete(() -> {
                    sendEvent(emitter, "done", "[DONE]");
                    emitter.complete();
                })
                .doOnError(ex -> {
                    sendEvent(emitter, "error", ex.getMessage());
                    emitter.completeWithError(ex);
                })
                .subscribe();
        return emitter;
    }

    @GetMapping("/conversations")
    public ApiResponse<SupportConversationPageResponse> listConversations(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        Pageable pageable = PageRequest.of(safePage(page), safeSize(size));
        Page<SupportConversationSummary> summaryPage = supportChatMessageRepository.findConversationSummaries(pageable);
        List<SupportConversationSummaryResponse> items = summaryPage.getContent().stream()
                .map(s -> new SupportConversationSummaryResponse(
                        s.getConversationId(),
                        s.getLastMessageAt(),
                        s.getMessageCount()
                ))
                .toList();
        return ApiResponse.ok("历史对话列表查询成功", new SupportConversationPageResponse(
                summaryPage.getTotalElements(),
                summaryPage.getTotalPages(),
                summaryPage.getNumber() + 1,
                summaryPage.getSize(),
                items
        ));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ApiResponse<List<SupportChatMessageResponse>> listMessages(@PathVariable String conversationId) {
        List<SupportChatMessageResponse> items = supportChatMessageRepository.findByConversationIdOrderByIdAsc(conversationId)
                .stream()
                .map(this::toMessageResponse)
                .toList();
        return ApiResponse.ok("历史对话消息查询成功", items);
    }

    private void sendEvent(SseEmitter emitter, String eventName, String data) {
        try {
            emitter.send(SseEmitter.event().name(eventName).data(data));
        } catch (IOException ex) {
            throw new IllegalStateException("流式响应写出失败", ex);
        }
    }

    private SupportChatMessageResponse toMessageResponse(SupportChatMessage msg) {
        return new SupportChatMessageResponse(
                msg.getId(),
                msg.getRole().name(),
                msg.getContent(),
                msg.getCreatedAt()
        );
    }

    private int safePage(Integer page) {
        if (page == null) {
            return 0;
        }
        int oneBased = Math.max(page, 1);
        return oneBased - 1;
    }

    private int safeSize(Integer size) {
        if (size == null || size < 1) {
            return 10;
        }
        return Math.min(size, 50);
    }
}
