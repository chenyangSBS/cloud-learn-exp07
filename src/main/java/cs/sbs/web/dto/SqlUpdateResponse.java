package cs.sbs.web.dto;

import java.math.BigDecimal;

public record SqlUpdateResponse(
        Long categoryId,
        BigDecimal delta,
        int affectedRows) {
}
