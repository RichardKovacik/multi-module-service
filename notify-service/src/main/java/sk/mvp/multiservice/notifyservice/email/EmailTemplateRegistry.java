package sk.mvp.multiservice.notifyservice.email;

import sk.mvp.common.event.EventType;

import java.util.Map;
import java.util.Optional;

/**
 * Registry class responsible for mapping core system EventTypes to their corresponding EmailNotificationTypes.
 * This decouples infrastructure/domain events from the presentation layer (email templates and localization keys),
 */
public class EmailTemplateRegistry {
    private static final Map<EventType, EmailNotificationType> REGISTRY = Map.of(
            EventType.USER_REGISTERED, EmailNotificationType.REGISTRATION_VERIFICATION,
            EventType.PASSWORD_RESET_REQUESTED, EmailNotificationType.PASSWORD_RESET
    );

    public static Optional<EmailNotificationType> getEmailNotificationTypeFor(EventType eventType) {
        return Optional.ofNullable(REGISTRY.get(eventType));
    }
}
