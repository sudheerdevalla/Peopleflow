package com.hr.hrapp.repository;

import com.hr.hrapp.entity.FinancialAccessOtp;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FinancialAccessOtpRepository extends JpaRepository<FinancialAccessOtp, Long> {

    Optional<FinancialAccessOtp> findTopByUsernameAndActiveTrueOrderByCreatedAtDesc(String username);
}
