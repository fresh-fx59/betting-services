package com.betting.betting_services.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JackpotContributionDto {

    private Long id;

    @NotNull(message = "Bet ID is required")
    private Long betId;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Jackpot ID is required")
    private Long jackpotId;

    @NotNull(message = "Stake amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Stake amount must be greater than 0")
    private BigDecimal stakeAmount;

    @NotNull(message = "Contribution amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Contribution amount must be greater than 0")
    private BigDecimal contributionAmount;

    @NotNull(message = "Current jackpot amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Current jackpot amount must be greater than 0")
    private BigDecimal currentJackpotAmount;

    private LocalDateTime createdAt;
}
