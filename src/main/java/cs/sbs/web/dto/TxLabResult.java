package cs.sbs.web.dto;

import java.util.Map;

public record TxLabResult(String scenario, Map<String, Object> details) {
}
