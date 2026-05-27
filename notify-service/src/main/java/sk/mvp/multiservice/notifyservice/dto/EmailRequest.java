package sk.mvp.multiservice.notifyservice.dto;

import java.util.Map;

public record EmailRequest(String to,
                           String subject,
                           String templateName,
                           Map<String, Object> templateModel// template data
) {
}
