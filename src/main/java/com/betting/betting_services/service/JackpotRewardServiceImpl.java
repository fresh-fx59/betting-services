package com.betting.betting_services.service;

import com.betting.betting_services.entity.Jackpot;
import com.betting.betting_services.entity.JackpotReward;
import com.betting.betting_services.model.BetDto;
import com.betting.betting_services.model.JackpotRewardDto;
import com.betting.betting_services.repository.JackpotRewardRepository;
import com.betting.betting_services.repository.JackpotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class JackpotRewardServiceImpl implements JackpotRewardService {

    private final JackpotRewardRepository jackpotRewardRepository;
    private final JackpotRepository jackpotRepository;

    @Override
    @Transactional
    public Optional<JackpotRewardDto> evaluate(BetDto bet) {
        log.info("Evaluating jackpot reward eligibility for bet ID: {}, jackpot ID: {}",
                bet.getId(), bet.getJackpotId());

        // Find the jackpot
        Optional<Jackpot> jackpotOptional = jackpotRepository.findById(bet.getJackpotId());

        if (jackpotOptional.isEmpty()) {
            log.warn("Jackpot not found with ID: {}. Skipping reward evaluation.", bet.getJackpotId());
            return Optional.empty();
        }

        Jackpot jackpot = jackpotOptional.get();

        // Evaluate eligibility using strategy from enum
        boolean isEligible = jackpot.getJackpotRewardType()
                .getStrategy()
                .isEligibleForReward(
                        jackpot.getCurrentJackpotValue(),
                        jackpot.getInitialJackpotValue(),
                        jackpot.getMaxJackpotValue()
                );

        if (!isEligible) {
            log.debug("Bet ID: {} not eligible for jackpot reward", bet.getId());
            return Optional.empty();
        }

        // Bet is eligible! Create reward record
        JackpotReward reward = JackpotReward.builder()
                .betId(bet.getId())
                .userId(bet.getUserId())
                .jackpotId(jackpot.getId())
                .jackpotRewardAmount(jackpot.getCurrentJackpotValue())
                .build();

        JackpotReward savedReward = jackpotRewardRepository.save(reward);

        log.info("Jackpot reward created! User {} won {} from jackpot {}",
                bet.getUserId(), jackpot.getCurrentJackpotValue(), jackpot.getId());

        // Reset jackpot pool to initial value
        jackpot.setCurrentJackpotValue(jackpot.getInitialJackpotValue());
        jackpotRepository.save(jackpot);

        log.info("Jackpot {} reset to initial value: {}",
                jackpot.getId(), jackpot.getInitialJackpotValue());

        return Optional.of(JackpotRewardDto.builder()
                .id(savedReward.getId())
                .betId(savedReward.getBetId())
                .userId(savedReward.getUserId())
                .jackpotId(savedReward.getJackpotId())
                .jackpotRewardAmount(savedReward.getJackpotRewardAmount())
                .createdAt(savedReward.getCreatedAt())
                .build());
    }

    @Override
    public Optional<JackpotRewardDto> findById(Long id) {
        log.info("Finding jackpot reward by ID: {}", id);

        return jackpotRewardRepository.findById(id)
                .map(reward -> JackpotRewardDto.builder()
                        .id(reward.getId())
                        .betId(reward.getBetId())
                        .userId(reward.getUserId())
                        .jackpotId(reward.getJackpotId())
                        .jackpotRewardAmount(reward.getJackpotRewardAmount())
                        .createdAt(reward.getCreatedAt())
                        .build());
    }
}
