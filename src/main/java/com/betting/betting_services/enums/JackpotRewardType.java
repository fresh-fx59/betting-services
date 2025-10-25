package com.betting.betting_services.enums;

import com.betting.betting_services.service.strategy.FixedRewardStrategy;
import com.betting.betting_services.service.strategy.RewardStrategy;
import com.betting.betting_services.service.strategy.VariableRewardStrategy;
import lombok.Getter;

@Getter
public enum JackpotRewardType {
    FIXED(new FixedRewardStrategy()),
    VARIABLE(new VariableRewardStrategy());

    private final RewardStrategy strategy;

    JackpotRewardType(RewardStrategy strategy) {
        this.strategy = strategy;
    }
}
