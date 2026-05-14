package sk.mvp.user_service.outbox.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import sk.mvp.common.event.BaseEvent;
import sk.mvp.user_service.outbox.entity.OutBoxStatus;
import sk.mvp.user_service.outbox.entity.OutboxEvent;
import sk.mvp.user_service.outbox.factory.OutboxFactory;
import sk.mvp.user_service.outbox.repository.OutBoxRepository;
import sk.mvp.user_service.async.producer.IEventProducer;
import sk.mvp.user_service.common.exception.OutboxNotFoundException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class OutBoxServiceImpl implements IOutBoxService {
    private OutBoxRepository outBoxRepository;
    private OutboxFactory outboxFactory;
    private IEventProducer eventProducer;

    public OutBoxServiceImpl(OutBoxRepository outBoxRepository, OutboxFactory outboxFactory, IEventProducer eventProducer) {
        this.outBoxRepository = outBoxRepository;
        this.outboxFactory = outboxFactory;
        this.eventProducer = eventProducer;
    }

    @Override
    @Transactional
    public <T> void saveOutbox(BaseEvent<T> event) {
        OutboxEvent outboxEvent = outboxFactory.createPendingOutboxEvent(event);
        outBoxRepository.save(outboxEvent);

    }

    @Override
    public <T> BaseEvent<T> findOutboxById(UUID id) throws OutboxNotFoundException {
        OutboxEvent event = outBoxRepository.findById(id).orElseThrow(() -> new OutboxNotFoundException("Outbox event with ID " + id + " not found."));
        return outboxFactory.toBaseEvent(event);
    }

    //TODO: DTO obeject better approach not returning db object directly
//    @Override
//    public OutboxEvent findOutboxById(UUID id) {
//        return  outBoxRepository.findById(id).orElseThrow(() -> new OutboxNotFoundException("Outbox event with ID " + id + " not found."));
//    }

    @Override
    @Transactional
    public void markAsProcessed(UUID id) {
        int rowsChanged = outBoxRepository.markAsProcessed(id, OutBoxStatus.PROCESSED, Instant.now());
        if (rowsChanged > 0) {
            log.info("Outbox event {} successfully marked as PROCESSED in DB.", id);
        } else {
            log.warn("Outbox event {} status update skipped. Row was not in PENDING state or does not exist. (Possible race condition)", id );
        }
    }

    @Override
    @Transactional
    public <T> void processPendingOutboxEventsBatch(int batchSize) {
        //check if broker is up
        if (!eventProducer.isBrokerUp()){
            return;
        }
        // 1. SELECT FOR UPDATE SKIP LOCKED
        // Another instacnes skiiped locked rows
        List<OutboxEvent> events = outBoxRepository.findReadyForRetry(Instant.now(), PageRequest.of(0, batchSize));
        if (events.isEmpty()){
            log.info("Outbox Retry job: No Pending events founded");
            return;
        }
        for (OutboxEvent event : events) {
            try {
                BaseEvent<T> eventToBeSend = outboxFactory.toBaseEvent(event);
                eventProducer.produce(eventToBeSend.destinationTopic(),eventToBeSend);
                markAsProcessed(event.getEventId());

            }catch (Exception e) {
                //handle fail
                log.error("Retry send failed for {}. Leaving outbox event in PENDING state for retry. Error: {},",
                        event.getEventId(), e.getMessage(), e);
            }

        }
    }

}
