package com.betting.betting_services.service;

import com.betting.betting_services.entity.Bet;
import com.betting.betting_services.entity.OutboxEvent;
import com.betting.betting_services.model.BetMessage;
import com.betting.betting_services.model.BetRequest;
import com.betting.betting_services.model.BetResponse;
import com.betting.betting_services.repository.BetRepository;
import com.betting.betting_services.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BetServiceImpl implements BetService {

    private final BetRepository betRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public BetResponse placeBet(BetRequest request) {
        log.info("Placing bet for user: {}, jackpot: {}, amount: {}",
                request.getUserId(), request.getJackpotId(), request.getBetAmount());

        // Save bet to database
        Bet bet = Bet.builder()
                .userId(request.getUserId())
                .jackpotId(request.getJackpotId())
                .betAmount(request.getBetAmount())
                .build();

        Bet savedBet = betRepository.save(bet);
        log.info("Bet saved with ID: {}", savedBet.getId());

        // Create message for Kafka
        BetMessage message = BetMessage.builder()
                .betId(savedBet.getId())
                .userId(savedBet.getUserId())
                .jackpotId(savedBet.getJackpotId())
                .betAmount(savedBet.getBetAmount())
                .build();

        // Save to outbox table (will be published to Kafka asynchronously)
        try {
            String payload = objectMapper.writeValueAsString(message);

            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .aggregateType("Bet")
                    .aggregateId(savedBet.getId().toString())
                    .eventType("BetPlaced")
                    .payload(payload)
                    .build();

            outboxEventRepository.save(outboxEvent);
            log.info("Outbox event created for bet ID: {}", savedBet.getId());

        } catch (JsonProcessingException e) {
            log.error("Error serializing bet message", e);
            throw new RuntimeException("Failed to create outbox event", e);
        }

        return BetResponse.builder()
                .betId(savedBet.getId())
                .userId(savedBet.getUserId())
                .jackpotId(savedBet.getJackpotId())
                .betAmount(savedBet.getBetAmount())
                .createdAt(savedBet.getCreatedAt())
                .message("Bet placed successfully")
                .build();
    }
}
