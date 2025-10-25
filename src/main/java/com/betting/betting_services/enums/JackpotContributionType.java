package com.betting.betting_services.enums;

import com.betting.betting_services.service.strategy.ContributionStrategy;
import com.betting.betting_services.service.strategy.FixedContributionStrategy;
import com.betting.betting_services.service.strategy.PercentageContributionStrategy;
import lombok.Getter;

@Getter
public enum JackpotContributionType {
    FIXED(new FixedContributionStrategy()),
    PERCENTAGE(new PercentageContributionStrategy());

    private final ContributionStrategy strategy;

    JackpotContributionType(ContributionStrategy strategy) {
        this.strategy = strategy;
    }
}
