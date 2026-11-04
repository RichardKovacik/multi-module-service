package sk.mvp.multiservice.notifyservice.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import sk.mvp.common.event.BaseEvent;
import sk.mvp.common.event.EventType;
import sk.mvp.multiservice.notifyservice.service.IEmailProvider;

@Slf4j
@Component
public class AccountRestoreNotificationHandler implements INotificationHandler {
    private final IEmailProvider emailProvider;
    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;

    public AccountRestoreNotificationHandler(IEmailProvider emailProvider, ObjectMapper objectMapper, MessageSource messageSource) {
        this.emailProvider = emailProvider;
        this.objectMapper = objectMapper;
        this.messageSource = messageSource;
    }

    @Override
    public void handleNotification(BaseEvent<?> event) {
        log.info("Notification handler for account restore event: not implemented yet");

    }

    @Override
    public EventType getSupportedEventType() {
        return EventType.ACCOUNT_RESTORE_REQUESTED;
    }
}
