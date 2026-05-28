package sk.mvp.multiservice.notifyservice.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import sk.mvp.common.event.BaseEvent;
import sk.mvp.common.event.EventType;
import sk.mvp.common.payloads.UserRegisteredPayload;
import sk.mvp.multiservice.notifyservice.email.EmailNotificationType;
import sk.mvp.multiservice.notifyservice.email.EmailTemplateRegistry;
import sk.mvp.multiservice.notifyservice.dto.EmailRequest;
import sk.mvp.multiservice.notifyservice.service.IEmailProvider;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Component
@Slf4j
public class RegistrationNotificationHandler implements INotificationHandler {
    private final IEmailProvider emailProvider;
    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;


    public RegistrationNotificationHandler(IEmailProvider emailProvider, ObjectMapper objectMapper, MessageSource messageSource) {
        this.emailProvider = emailProvider;
        this.objectMapper = objectMapper;
        this.messageSource = messageSource;
    }

    //TODO: simplify based on eventType get obeject(template,subject) hashMap maybe
    @Override
    public void handleNotification(BaseEvent<?> event) {
        UserRegisteredPayload payload = objectMapper.convertValue(event.payload(), UserRegisteredPayload.class);
        log.info("Processing registration for user: {}", event.userId());
        // 1. Resolve presentation metadata from the central registry
        EmailNotificationType  emailNotificationType = EmailTemplateRegistry.getEmailNotificationTypeFor(event.eventType())
                .orElseThrow(() -> new IllegalArgumentException("No email mapping found for event: " + event.eventType()));
        //get subject of email
        String subject = messageSource.getMessage(emailNotificationType.getSubjectKey(), null, Locale.getDefault());

        //prepare request and template model
        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("verificationUrl", payload.link());

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
        return EventType.USER_REGISTERED;
    }
}
