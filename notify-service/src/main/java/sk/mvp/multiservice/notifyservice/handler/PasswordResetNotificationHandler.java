package sk.mvp.multiservice.notifyservice.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import sk.mvp.common.event.BaseEvent;
import sk.mvp.common.event.EventType;
import sk.mvp.common.payloads.PasswordResetPayload;
import sk.mvp.multiservice.notifyservice.email.EmailNotificationType;
import sk.mvp.multiservice.notifyservice.email.EmailTemplateRegistry;
import sk.mvp.multiservice.notifyservice.dto.EmailRequest;
import sk.mvp.multiservice.notifyservice.service.IEmailProvider;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Component
@Slf4j
public class PasswordResetNotificationHandler implements INotificationHandler {
    private final IEmailProvider emailProvider;
    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;

    public PasswordResetNotificationHandler(IEmailProvider emailProvider, ObjectMapper objectMapper, MessageSource messageSource) {
        this.emailProvider = emailProvider;
        this.objectMapper = objectMapper;
        this.messageSource = messageSource;
    }

    @Override
    public void handleNotification(BaseEvent<?> event) {
        PasswordResetPayload payload = objectMapper.convertValue(event.payload(), PasswordResetPayload.class);
        log.info("Processing password reset request for user: {}", event.userId());

        // 1. Resolve presentation metadata from the central registry
        EmailNotificationType emailNotificationType = EmailTemplateRegistry.getEmailNotificationTypeFor(event.eventType())
                .orElseThrow(() -> new IllegalArgumentException("No email mapping found for event: " + event.eventType()));

        //get subject of email
        String subject = messageSource.getMessage(emailNotificationType.getSubjectKey(), null, Locale.getDefault());

        //prepare request and template data
        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("resetUrl", payload.link());

        EmailRequest emailRequest = new EmailRequest(
                payload.email(),
                subject,
                emailNotificationType.getTemplateName(),
                templateModel
        );

        emailProvider.sendEmail(emailRequest);

    }

    @Override
    public EventType getSupportedEventType() {
        return EventType.PASSWORD_RESET_REQUESTED;
    }
}
