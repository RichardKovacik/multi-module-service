package sk.mvp.multiservice.notifyservice.handler;

import sk.mvp.common.event.BaseEvent;
import sk.mvp.common.event.EventType;

/**
 * Strategy pattern - each event type has its own handler
 */
public interface INotificationHandler {
    void handleNotification(BaseEvent<?> event);
    EventType getSupportedEventType();
}
