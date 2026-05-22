package sk.mvp.multiservice.notifyservice.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import sk.mvp.common.event.BaseEvent;
import sk.mvp.common.event.EventType;
import sk.mvp.common.payloads.PasswordResetPayload;
import sk.mvp.common.payloads.UserRegisteredPayload;
import sk.mvp.multiservice.notifyservice.dto.EmailRequest;
import sk.mvp.multiservice.notifyservice.service.IEmailProvider;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaUserEventListener {
    private final ObjectMapper objectMapper;
    private final IEmailProvider emailProvider;

    @KafkaListener(topics = "user-event-topic", groupId = "notification-group")
    public void consumeEvent(BaseEvent<?> event) {
        try {
            log.info("{} Started processing event: {} for user: {}",
                    event.metadata().correlationId(),
                    event.eventType(),
                    event.userId());

            EventType type = EventType.valueOf(event.eventType());

            // 2. Routing
            switch (type) {
                case USER_REGITERED_EVENT -> {
                    var payload = convert(event.payload(), UserRegisteredPayload.class);
                    handleRegistrationEvent(payload, event.userId());
                }
                case PASSWORD_CHANGE_REQUESTED_EVENT -> {
                    var payload = convert(event.payload(), PasswordResetPayload.class);
                    handlePasswordResetEvent(payload, event.userId());
                }
                case UNKNOWN_EVENT -> log.warn("{} Recieved unknown event type:",event.metadata().correlationId());
            }
        } catch (Exception e) {
            log.error("{} Exception in consuming message: {}", event.metadata().correlationId(), event.eventId(), e);
        } finally {
        }
    }

    private void handleRegistrationEvent(UserRegisteredPayload payload, String userId) {
        log.info("Registration user {} with email {}", userId, payload.email());

        //prepare request and template
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

    private void handlePasswordResetEvent(PasswordResetPayload payload, String userId) {
        log.info("Reset hesla pre email {}", payload.email());
    }

    private <T> T convert(Object payload, Class<T> clazz) {
        return objectMapper.convertValue(payload, clazz);
    }
}
