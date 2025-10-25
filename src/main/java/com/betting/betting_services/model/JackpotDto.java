package com.betting.betting_services.model;

import com.betting.betting_services.entity.Jackpot;
import com.betting.betting_services.enums.JackpotContributionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JackpotDto {

    private Long id;

    @NotNull(message = "Initial jackpot value is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Initial jackpot value must be greater than 0")
    private BigDecimal initialJackpotValue;

    @NotNull(message = "Current jackpot value is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Current jackpot value must be greater than 0")
    private BigDecimal currentJackpotValue;

    @NotNull(message = "Max jackpot value is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Max jackpot value must be greater than 0")
    private BigDecimal maxJackpotValue;

    @NotNull(message = "Jackpot contribution type is required")
    private JackpotContributionType jackpotContributionType;
}
