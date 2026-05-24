package sk.mvp.multiservice.notifyservice.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.nio.sctp.NotificationHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sk.mvp.common.event.BaseEvent;
import sk.mvp.common.event.EventType;
import sk.mvp.common.payloads.UserRegisteredPayload;
import sk.mvp.multiservice.notifyservice.dto.EmailRequest;
import sk.mvp.multiservice.notifyservice.service.IEmailProvider;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class RegistrationNotificationHandler implements INotificationHandler {
    private final IEmailProvider emailProvider;
    private final ObjectMapper objectMapper;

    public RegistrationNotificationHandler(IEmailProvider emailProvider, ObjectMapper objectMapper) {
        this.emailProvider = emailProvider;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handleNotification(BaseEvent<?> event) {
        UserRegisteredPayload payload = objectMapper.convertValue(event.payload(), UserRegisteredPayload.class);
        log.info("Processing registration for user: {}", event.userId());
        //prepare request and template model
        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("verificationUrl", payload.link());

        EmailRequest emailRequest = new EmailRequest(
                payload.email(),                           // to
                "Welcome! Please Verify Your Email",       // subject
                "registration-verification",               // name HTML tempalte
                templateModel                              // DataModel for template thymeleaf
        );

        emailProvider.sendEmail(emailRequest);

    }

    @Override
    public EventType getSupportedEventType() {
        return EventType.USER_REGISTERED;
    }
}
