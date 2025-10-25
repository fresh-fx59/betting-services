package com.betting.betting_services.service;

import com.betting.betting_services.entity.OutboxEvent;
import com.betting.betting_services.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OutboxPublisher {

    private static final int MAX_RETRIES = 3;

    @Value("${kafka.topic.jackpot-bets}")
    private String jackpotBetsTopic;

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 5000) // Run every 5 seconds
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findPendingEvents();

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("Found {} pending outbox events to publish", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            try {
                // Publish to Kafka
                kafkaTemplate.send(jackpotBetsTopic, event.getAggregateId(), event.getPayload())
                        .whenComplete((result, ex) -> {
                            if (ex == null) {
                                markAsPublished(event);
                                log.info("Successfully published event ID: {} to topic: {}",
                                        event.getId(), jackpotBetsTopic);
                            } else {
                                handlePublishFailure(event, ex);
                            }
                        });

            } catch (Exception e) {
                handlePublishFailure(event, e);
            }
        }
    }

    @Transactional
    protected void markAsPublished(OutboxEvent event) {
        event.setStatus(OutboxEvent.EventStatus.PUBLISHED);
        event.setPublishedAt(LocalDateTime.now());
        outboxEventRepository.save(event);
    }

    @Transactional
    protected void handlePublishFailure(OutboxEvent event, Throwable error) {
        event.setRetryCount(event.getRetryCount() + 1);
        event.setErrorMessage(error.getMessage());

        if (event.getRetryCount() >= MAX_RETRIES) {
            event.setStatus(OutboxEvent.EventStatus.FAILED);
            log.error("Event ID: {} failed after {} retries. Marking as FAILED.",
                    event.getId(), MAX_RETRIES);
        } else {
            log.warn("Event ID: {} failed. Retry count: {}",
                    event.getId(), event.getRetryCount());
        }

        outboxEventRepository.save(event);
    }
}
