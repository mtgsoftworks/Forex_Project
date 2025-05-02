package com.example.forexproject.model;

public class RateFields {
    private double bid;
    private double ask;

    public RateFields(double bid, double ask) {
        this.bid = bid;
        this.ask = ask;
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
}

