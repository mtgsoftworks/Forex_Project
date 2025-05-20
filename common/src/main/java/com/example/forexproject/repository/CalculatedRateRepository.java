package com.example.forexproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.forexproject.model.CalculatedRateEntity;

public interface CalculatedRateRepository extends JpaRepository<CalculatedRateEntity, Long> {
} 