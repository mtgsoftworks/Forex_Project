package com.example.forexproject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * DTO for transferring Rate data with validation.
 */
public class RateDto {

    @NotBlank(message = "Rate name cannot be blank")
    private String rateName;

    @PositiveOrZero(message = "Bid must be zero or positive")
    private double bid;

    @PositiveOrZero(message = "Ask must be zero or positive")
    private double ask;

    @NotBlank(message = "Timestamp cannot be blank")
    private String timestamp;

    public String getRateName() {
        return rateName;
    }

    public void setRateName(String rateName) {
        this.rateName = rateName;
    }

    public double getBid() {
        return bid;
    }

    public void setBid(double bid) {
        this.bid = bid;
    }

    public double getAsk() {
        return ask;
    }

    public void setAsk(double ask) {
        this.ask = ask;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
