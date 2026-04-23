package cs.sbs.web.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CourseBatchPublishRequest(
        @NotEmpty(message = "课程ID列表不能为空")
        List<Long> courseIds,
        @NotNull(message = "发布状态不能为空")
        Boolean published) {
}
