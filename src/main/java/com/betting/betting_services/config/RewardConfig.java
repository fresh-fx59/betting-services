package com.betting.betting_services.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Getter
public class RewardConfig {

    private static BigDecimal fixedRewardChance;
    private static BigDecimal variableInitialRewardChance;
    private static BigDecimal variableGrowthRate;

    @Value("${jackpot.reward.fixed.chance}")
    public void setFixedRewardChance(BigDecimal value) {
        RewardConfig.fixedRewardChance = value;
    }

    @Value("${jackpot.reward.variable.initial-chance}")
    public void setVariableInitialRewardChance(BigDecimal value) {
        RewardConfig.variableInitialRewardChance = value;
    }

    @Value("${jackpot.reward.variable.growth-rate}")
    public void setVariableGrowthRate(BigDecimal value) {
        RewardConfig.variableGrowthRate = value;
    }

    public static BigDecimal getFixedRewardChanceValue() {
        return fixedRewardChance;
    }

    public static BigDecimal getVariableInitialRewardChanceValue() {
        return variableInitialRewardChance;
    }

    public static BigDecimal getVariableGrowthRateValue() {
        return variableGrowthRate;
    }
}
