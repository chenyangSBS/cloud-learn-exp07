package cs.sbs.web.service;

import java.util.Map;
import reactor.core.publisher.Flux;

public interface AiSupportService {

    Flux<String> streamReply(
            String conversationId,
            String question,
            boolean reset,
            Map<String, Object> extraBody
    );
}
