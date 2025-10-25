package com.betting.betting_services.service;

import com.betting.betting_services.entity.Jackpot;
import com.betting.betting_services.entity.JackpotContribution;
import com.betting.betting_services.model.BetDto;
import com.betting.betting_services.model.JackpotContributionDto;
import com.betting.betting_services.repository.JackpotContributionRepository;
import com.betting.betting_services.repository.JackpotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class JackpotContributionServiceImpl implements JackpotContributionService {

    private final JackpotContributionRepository jackpotContributionRepository;
    private final JackpotRepository jackpotRepository;

    @Override
    @Transactional
    public Optional<JackpotContributionDto> contribute(BetDto bet) {
        log.info("Processing jackpot contribution for bet ID: {}, jackpot ID: {}",
                bet.getId(), bet.getJackpotId());

        // Find the matching jackpot based on Jackpot ID
        Optional<Jackpot> jackpotOptional = jackpotRepository.findById(bet.getJackpotId());

        if (jackpotOptional.isEmpty()) {
            log.warn("Jackpot not found with ID: {}. Skipping contribution.", bet.getJackpotId());
            return Optional.empty();
        }

        Jackpot jackpot = jackpotOptional.get();

        // Calculate contribution using strategy from enum
        BigDecimal contributionAmount = jackpot.getJackpotContributionType()
                .getStrategy()
                .calculate(bet.getBetAmount(), jackpot.getCurrentJackpotValue(), jackpot.getInitialJackpotValue());

        // Update jackpot current value
        BigDecimal newJackpotValue = jackpot.getCurrentJackpotValue().add(contributionAmount);
        jackpot.setCurrentJackpotValue(newJackpotValue);
        jackpotRepository.save(jackpot);

        // Create jackpot contribution record
        JackpotContribution contribution = JackpotContribution.builder()
                .betId(bet.getId())
                .userId(bet.getUserId())
                .jackpotId(jackpot.getId())
                .stakeAmount(bet.getBetAmount())
                .contributionAmount(contributionAmount)
                .currentJackpotAmount(newJackpotValue)
                .build();

        JackpotContribution savedContribution = jackpotContributionRepository.save(contribution);

        log.info("Jackpot contribution created: {} contributed to jackpot {}, new total: {}",
                contributionAmount, jackpot.getId(), newJackpotValue);

        return Optional.of(JackpotContributionDto.builder()
                .id(savedContribution.getId())
                .betId(savedContribution.getBetId())
                .userId(savedContribution.getUserId())
                .jackpotId(savedContribution.getJackpotId())
                .stakeAmount(savedContribution.getStakeAmount())
                .contributionAmount(savedContribution.getContributionAmount())
                .currentJackpotAmount(savedContribution.getCurrentJackpotAmount())
                .createdAt(savedContribution.getCreatedAt())
                .build());
    }
}
