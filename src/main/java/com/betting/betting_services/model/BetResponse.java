package com.betting.betting_services.model;

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
public class BetResponse {

    private Long betId;
    private Long userId;
    private Long jackpotId;
    private BigDecimal betAmount;
    private LocalDateTime createdAt;
    private String message;
}
