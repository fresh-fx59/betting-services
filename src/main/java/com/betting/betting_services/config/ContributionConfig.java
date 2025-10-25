package com.betting.betting_services.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Getter
public class ContributionConfig {

    private static BigDecimal fixedPercentage;
    private static BigDecimal variableInitialPercentage;
    private static BigDecimal variableDecayRate;

    @Value("${jackpot.contribution.fixed.percentage}")
    public void setFixedPercentage(BigDecimal value) {
        ContributionConfig.fixedPercentage = value;
    }

    @Value("${jackpot.contribution.variable.initial-percentage}")
    public void setVariableInitialPercentage(BigDecimal value) {
        ContributionConfig.variableInitialPercentage = value;
    }

    @Value("${jackpot.contribution.variable.decay-rate}")
    public void setVariableDecayRate(BigDecimal value) {
        ContributionConfig.variableDecayRate = value;
    }

    public static BigDecimal getFixedPercentageValue() {
        return fixedPercentage;
    }

    public static BigDecimal getVariableInitialPercentageValue() {
        return variableInitialPercentage;
    }

    public static BigDecimal getVariableDecayRateValue() {
        return variableDecayRate;
    }
}
