package com.example.forexproject.coordinator.provider;

import com.example.forexproject.coordinator.CoordinatorCallback;

/**
 * Interface that every platform‐specific data provider must implement.
 * Each provider is expected to run in its own thread and communicate back
 * to the {@link com.example.forexproject.coordinator.service.CoordinatorService}
 * via the supplied {@link CoordinatorCallback}.
 */
// Removed duplicate package declaration
public interface DataProvider extends Runnable {

    /* Inject callback reference before the provider is started */
    void setCallback(CoordinatorCallback callback);

    // Establish connection towards the remote platform
    void connect(String platformName, String userId, String password);

    // Close connection gracefully
    void disconnect(String platformName, String userId, String password);

    // Subscribe to a given rate
    void subscribe(String platformName, String rateName);

    // Unsubscribe from a given rate
    void unSubscribe(String platformName, String rateName);

    // Lifecycle helpers (can be empty NOP for most implementations)
    void startProvider();
    void stopProvider();
}