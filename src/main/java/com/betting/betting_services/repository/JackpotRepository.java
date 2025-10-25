package com.betting.betting_services.repository;

import com.betting.betting_services.entity.Jackpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JackpotRepository extends JpaRepository<Jackpot, Long> {
}
