package sk.mvp.user_service.outbox.dto;

import java.util.UUID;

public record OutboxTriggerEvent(UUID eventId) {
}
