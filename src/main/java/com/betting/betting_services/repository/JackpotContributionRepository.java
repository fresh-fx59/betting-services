package com.betting.betting_services.repository;

import com.betting.betting_services.entity.JackpotContribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JackpotContributionRepository extends JpaRepository<JackpotContribution, Long> {
}
