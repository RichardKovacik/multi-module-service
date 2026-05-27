package sk.mvp.user_service.outbox.service;

import sk.mvp.common.event.BaseEvent;
import sk.mvp.user_service.common.exception.OutboxNotFoundException;

import java.util.UUID;

public interface IOutBoxService {
    void saveOutbox(BaseEvent<?> event);
    BaseEvent<?> findOutboxById(UUID id) throws OutboxNotFoundException;
    void markAsProcessed(UUID id);
    void processPendingOutboxEventsBatch(int batchSize);


}
