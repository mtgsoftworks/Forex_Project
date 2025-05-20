package com.example.forexproject.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "calculated_data")
public class CalculatedRateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String rateName;
    private double bid;
    private double ask;
    private LocalDateTime rateUpdateTime;
    private LocalDateTime dbUpdateTime;

    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRateName() { return rateName; }
    public void setRateName(String rateName) { this.rateName = rateName; }
    public double getBid() { return bid; }
    public void setBid(double bid) { this.bid = bid; }
    public double getAsk() { return ask; }
    public void setAsk(double ask) { this.ask = ask; }
    public LocalDateTime getRateUpdateTime() { return rateUpdateTime; }
    public void setRateUpdateTime(LocalDateTime rateUpdateTime) { this.rateUpdateTime = rateUpdateTime; }
    public LocalDateTime getDbUpdateTime() { return dbUpdateTime; }
    public void setDbUpdateTime(LocalDateTime dbUpdateTime) { this.dbUpdateTime = dbUpdateTime; }
} 