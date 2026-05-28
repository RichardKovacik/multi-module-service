package sk.mvp.common.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sk.mvp.common.event.BaseEvent;
import sk.mvp.common.event.EventMetadata;
import sk.mvp.common.event.EventType;
import sk.mvp.common.payloads.PasswordResetPayload;
import sk.mvp.common.payloads.UserRegisteredPayload;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserEventFactory {
    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${app.events.version:1}")
    private int eventVersion;

    public BaseEvent<UserRegisteredPayload> createUserRegisteredEvent(String email,
                                                                      String link,
                                                                      String userId,
                                                                      String correlationId,
                                                                      String destinationTopic) {
        return BaseEvent.<UserRegisteredPayload>builder()
                .eventId(UUID.randomUUID())
                .createdAt(Instant.now())
                .eventType(EventType.USER_REGISTERED)
                .metadata(EventMetadata.builder()
                        .correlationId(correlationId)
                        .sourceService(applicationName)
                        .build())
                .eventVersion(eventVersion)
                .userId(userId)
                .destinationTopic(destinationTopic)
                .payload(UserRegisteredPayload.builder()
                        .email(email)
                        .link(link)
                        .build())
                .build();
    }

    public BaseEvent<PasswordResetPayload> createPasswordResetRequestedEvent(String email,
                                                                            String link,
                                                                            String userId,
                                                                              String correlationId,
                                                                              String destinationTopic) {
        return BaseEvent.<PasswordResetPayload>builder()
                .eventId(UUID.randomUUID())
                .createdAt(Instant.now())
                .eventType(EventType.PASSWORD_RESET_REQUESTED)
                .metadata(EventMetadata.builder()
                        .correlationId(correlationId)
                        .sourceService(applicationName)
                        .build())
                .eventVersion(eventVersion)
                .destinationTopic(destinationTopic)
                .userId(userId)
                .payload(PasswordResetPayload.builder()
                        .email(email)
                        .link(link)
                        .build())
                .build();
    }
}
