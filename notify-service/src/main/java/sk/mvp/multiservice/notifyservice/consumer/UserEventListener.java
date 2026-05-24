package sk.mvp.multiservice.notifyservice.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import sk.mvp.common.event.EventType;
import sk.mvp.common.payloads.PasswordResetPayload;
import sk.mvp.common.payloads.UserRegisteredPayload;
import sk.mvp.common.event.BaseEvent;
import sk.mvp.multiservice.notifyservice.dto.EmailRequest;
import sk.mvp.multiservice.notifyservice.handler.INotificationHandler;
import sk.mvp.multiservice.notifyservice.service.IEmailProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class UserEventListener {
    private final Map<EventType, INotificationHandler> handlerMap;

    public UserEventListener(List<INotificationHandler> handlers) {
        this.handlerMap = handlers.stream()
                .collect(Collectors.toMap(INotificationHandler::getSupportedEventType, h -> h));
    }

    @KafkaListener(topics = "user-event-topic", groupId = "notification-group")
    public void consumeEvent(BaseEvent<?> event) {
        try {
            log.info("{} Started processing event: {} for user: {}",
                    event.metadata().correlationId(),
                    event.eventType(),
                    event.userId());

            // Smart routing used strategy pattern
            EventType type = EventType.valueOf(event.eventType());
            INotificationHandler handler = handlerMap.get(type);

            if (handler != null) {
                handler.handleNotification(event);
            } else {
                log.warn("{} No handler found for event type: {}", event.metadata().correlationId(), event.eventType());
            }
        } catch (Exception e) {
            log.error("{} Exception in consuming message: {}", event.metadata().correlationId(), event.eventId(), e);
        }
    }

}
