package sk.mvp.common.event;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;
import java.util.UUID;


@Builder
@Jacksonized
public record BaseEvent<T>(
        UUID eventId,
        EventType eventType,
        int eventVersion,
        Instant createdAt,
        String userId,
        EventMetadata metadata,
        String destinationTopic,
        T payload
) {}
