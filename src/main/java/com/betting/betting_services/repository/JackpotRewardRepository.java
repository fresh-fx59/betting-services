package com.betting.betting_services.repository;

import com.betting.betting_services.entity.JackpotReward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JackpotRewardRepository extends JpaRepository<JackpotReward, Long> {
}
