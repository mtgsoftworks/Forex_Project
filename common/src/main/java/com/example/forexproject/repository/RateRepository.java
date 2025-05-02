package com.example.forexproject.repository;

import com.example.forexproject.model.RateEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RateRepository extends JpaRepository<RateEntity, Long> {
    @Query("SELECT r FROM RateEntity r WHERE r.rateUpdateTime >= :startTime")
    List<RateEntity> findRatesUpdatedAfter(@Param("startTime") LocalDateTime startTime);

    @Query("SELECT r FROM RateEntity r WHERE r.rateName = ?1 ORDER BY r.rateUpdateTime DESC")
    List<RateEntity> findLatestRateByRateName(String rateName, Pageable pageable);
}
