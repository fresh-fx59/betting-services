package com.betting.betting_services.service;

import com.betting.betting_services.model.BetDto;
import com.betting.betting_services.model.JackpotRewardDto;

import java.util.Optional;

public interface JackpotRewardService {

    Optional<JackpotRewardDto> evaluate(BetDto bet);

    Optional<JackpotRewardDto> findById(Long id);
}
