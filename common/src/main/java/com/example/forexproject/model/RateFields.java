package com.example.forexproject.model;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RateFields)) return false;
        RateFields that = (RateFields) o;
        return Double.compare(that.bid, bid) == 0 &&
               Double.compare(that.ask, ask) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bid, ask);
    }
}
