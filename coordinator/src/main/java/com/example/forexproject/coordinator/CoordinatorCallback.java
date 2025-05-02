package com.example.forexproject.coordinator;

import com.example.forexproject.model.Rate;
import com.example.forexproject.model.RateFields;
import com.example.forexproject.model.RateStatus;

public interface CoordinatorCallback {
    void onConnect(String platformName, boolean status);
    void onDisconnect(String platformName, boolean status);
    void onRateAvailable(String platformName, String rateName, Rate rate);
    void onRateUpdate(String platformName, String rateName, RateFields rateFields);
    void onRateStatus(String platformName, String rateName, RateStatus rateStatus);
}
