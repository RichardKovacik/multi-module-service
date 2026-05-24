package sk.mvp.user_service.outbox.factory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sk.mvp.common.event.BaseEvent;
import sk.mvp.common.event.EventType;
import sk.mvp.common.payloads.PasswordResetPayload;
import sk.mvp.common.payloads.UserRegisteredPayload;
import sk.mvp.user_service.outbox.entity.OutBoxStatus;
import sk.mvp.user_service.outbox.entity.OutboxEvent;

import java.time.Instant;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OutboxFactory {
    private final ObjectMapper objectMapper; // Jackson


    private static final Map<String, Class<?>> TYPE_MAP = Map.of(
            EventType.USER_REGISTERED.toString(), UserRegisteredPayload.class,
            EventType.PASSWORD_RESET_REQUESTED.toString(), PasswordResetPayload.class
            // Tu pridáš ďalšie eventy
    );

    public Class<?> getPayloadClass(String eventType) {
        Class<?> clazz = TYPE_MAP.get(eventType);
        if (clazz == null) {
            throw new IllegalArgumentException("Unknown event type: " + eventType);
        }
        return clazz;
    }

    public <T> BaseEvent<T> toBaseEvent(OutboxEvent entity) {
        try {
            // 1. Zisti, aký typ payloadu to má byť (napr. UserRegisteredPayload.class)
            Class<?> payloadClass = getPayloadClass(entity.getEventType());

            // 2. Vybuduj plný generický typ: BaseEvent<PayloadClass>
            JavaType fullType = objectMapper.getTypeFactory()
                    .constructParametricType(BaseEvent.class, payloadClass);

            // 3. Deserializuj JSON string na kompletný objekt
            return objectMapper.readValue(entity.getPayload(), fullType);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize Outbox payload for event " + entity.getEventId(), e);
        }
    }

    /**
     * It creates Outbox entity
     * bridge the Domain and Infrastructure layers.
     */
    public <T> OutboxEvent createPendingOutboxEvent(BaseEvent<T> event) {
        try {
            // Serialize the entire BaseEvent object to JSON for the 'payload' column
            String jsonPayload = objectMapper.writeValueAsString(event);

            return OutboxEvent.builder()
                    .eventId(event.eventId())
                    .correlationId(event.metadata().correlationId())
                    .eventType(event.eventType())
                    .aggregateId(event.userId())
                    .payload(jsonPayload)
                    .destinationTopic(event.destinationTopic())
                    .status(OutBoxStatus.PENDING)
                    .retryCount(0)
                    .nextRetryAt(Instant.now().plusSeconds(60))
                    .createdAt(event.createdAt())
                    .build();
        } catch (JsonProcessingException e) {
            // As a Senior Dev, never swallow this. Wrap it in a RuntimeException
            // to trigger a transaction rollback.
            throw new RuntimeException("Critical: Failed to serialize event for Outbox. ID: " + event.eventId(), e);
        }
    }


}
