package com.betting.betting_services.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "jackpot_contributions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JackpotContribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long betId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long jackpotId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal stakeAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal contributionAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal currentJackpotAmount;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
