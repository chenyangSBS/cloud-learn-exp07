package cs.sbs.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CourseCreateRequest(
        @NotBlank(message = "课程标题不能为空")
        @Size(max = 100, message = "课程标题长度不能超过100")
        String title,
        @NotBlank(message = "讲师不能为空")
        @Size(max = 50, message = "讲师名称长度不能超过50")
        String teacher,
        @Size(max = 500, message = "课程简介长度不能超过500")
        String summary,
        @NotNull(message = "课程价格不能为空")
        @DecimalMin(value = "0.0", inclusive = true, message = "课程价格不能小于0")
        BigDecimal price,
        @NotNull(message = "课程课时不能为空")
        @Positive(message = "课程课时必须大于0")
        Integer lessonCount,
        @NotNull(message = "分类ID不能为空")
        Long categoryId,
        Boolean published) {
}
