package sk.mvp.user_service.outbox.repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import sk.mvp.user_service.outbox.entity.OutBoxStatus;
import sk.mvp.user_service.outbox.entity.OutboxEvent;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutBoxRepository extends JpaRepository<OutboxEvent, UUID> {
    @Modifying
    @Query("UPDATE OutboxEvent e SET e.status = :status, e.processedAt = :now " +
            "WHERE e.eventId = :id AND e.status = 'PENDING'") // Atomic safety check
    int markAsProcessed(@Param("id") UUID id,
                        @Param("status") OutBoxStatus status,
                        @Param("now") Instant now);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2")}) // "-2" tells Hibernate to use SKIP LOCKED in Postgres
    @Query("SELECT e FROM OutboxEvent e " +
            "WHERE e.status = 'PENDING' AND e.nextRetryAt <= :now " +
            "ORDER BY e.nextRetryAt ASC")
    List<OutboxEvent> findReadyForRetry(@Param("now") Instant now, Pageable pageable);

    @Modifying
    @Query("DELETE FROM OutboxEvent e WHERE e.status = 'PROCESSED' AND e.processedAt < :threshold")
    int deleteOldProcessed(@Param("threshold") Instant threshold);
}
