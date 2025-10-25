package com.betting.betting_services.entity;

import com.betting.betting_services.enums.JackpotContributionType;
import com.betting.betting_services.enums.JackpotRewardType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "jackpots")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Jackpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal initialJackpotValue;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal currentJackpotValue;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal maxJackpotValue;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private JackpotContributionType jackpotContributionType;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private JackpotRewardType jackpotRewardType;
}
