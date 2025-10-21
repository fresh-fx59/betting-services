package com.betting.betting_services.repository;

import com.betting.betting_services.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Query("SELECT o FROM OutboxEvent o WHERE o.status = 'PENDING' OR (o.status = 'FAILED' AND o.retryCount < 3) ORDER BY o.createdAt ASC")
    List<OutboxEvent> findPendingEvents();
}
