package sk.mvp.user_service.outbox.service;

import sk.mvp.common.event.BaseEvent;
import sk.mvp.user_service.common.exception.OutboxNotFoundException;

import java.util.UUID;

public interface IOutBoxService {
    <T>void saveOutbox(BaseEvent<T> event);
    <T> BaseEvent<T> findOutboxById(UUID id) throws OutboxNotFoundException;
    void markAsProcessed(UUID id);
    <T> void processPendingOutboxEventsBatch(int batchSize);


}
