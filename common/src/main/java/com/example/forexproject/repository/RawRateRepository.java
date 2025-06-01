package com.example.forexproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.forexproject.model.RawRateEntity;

public interface RawRateRepository extends JpaRepository<RawRateEntity, Long> {
} 