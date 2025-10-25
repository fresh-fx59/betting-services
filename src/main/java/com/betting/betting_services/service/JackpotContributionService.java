package com.betting.betting_services.service;

import com.betting.betting_services.model.BetDto;
import com.betting.betting_services.model.JackpotContributionDto;

import java.util.Optional;

public interface JackpotContributionService {

    Optional<JackpotContributionDto> contribute(BetDto bet);
}
