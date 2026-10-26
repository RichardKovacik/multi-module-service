package sk.mvp.multiservice.notifyservice.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sk.mvp.common.event.BaseEvent;
import sk.mvp.common.event.EventType;

@Slf4j
@Component
public class AccountRestoreNotificationHandler implements INotificationHandler {
    @Override
    public void handleNotification(BaseEvent<?> event) {
        log.info("Notification handler for account restore event: not implemented yet");

    }

    @Override
    public EventType getSupportedEventType() {
        return EventType.ACCOUNT_RESTORE_REQUESTED;
    }
}
