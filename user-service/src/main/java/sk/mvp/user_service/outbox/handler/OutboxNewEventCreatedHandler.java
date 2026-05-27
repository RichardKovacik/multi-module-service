package sk.mvp.user_service.outbox.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import sk.mvp.common.event.BaseEvent;
import sk.mvp.user_service.outbox.dto.OutboxTriggerEvent;
import sk.mvp.user_service.outbox.service.IOutBoxService;
import sk.mvp.user_service.async.producer.IEventProducer;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxNewEventCreatedHandler {
    private final IOutBoxService outBoxService;
    private final IEventProducer eventProducer;

    @Async("outboxExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNewOutboxEventCreated(OutboxTriggerEvent event) {
        log.info("Handling internal event new outbox created: {}", event.eventId());
        try {
            BaseEvent<?> deserializedPayload = outBoxService.findOutboxById(event.eventId());

            eventProducer.produce(deserializedPayload.destinationTopic(), deserializedPayload);
            log.info("[{}] Event {} sucessefully to kafka", deserializedPayload.metadata().correlationId(), event.eventId());
            outBoxService.markAsProcessed(event.eventId());

        }catch (Exception e) {
            log.error("Immediate send failed for {}. Leaving outbox event in PENDING state for retry. Error: {},",
                    event.eventId(), e.getMessage(), e);
        }

    }
}
