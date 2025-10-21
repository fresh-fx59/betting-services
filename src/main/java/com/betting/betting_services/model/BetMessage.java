package com.betting.betting_services.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BetMessage {

    private Long betId;
    private Long userId;
    private Long jackpotId;
    private BigDecimal betAmount;
}
