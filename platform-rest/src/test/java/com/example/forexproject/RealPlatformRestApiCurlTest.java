package com.example.forexproject;

import org.junit.jupiter.api.Test;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RealPlatformRestApiCurlTest {

    @Test
    public void testCurlRequestsForAllSymbols() throws IOException, InterruptedException {
        // Define the rate names to test
        String[] rateNames = {"PF2_GBPUSD", "PF2_EURUSD", "PF2_USDTRY"};
        for (String rateName : rateNames) {
            ProcessBuilder processBuilder = new ProcessBuilder("curl", "http://localhost:8081/api/rates/" + rateName);
            Process process = processBuilder.start();

            // Read the output of the curl command
            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            System.out.println("Curl exit code for " + rateName + ": " + exitCode);
            System.out.println("Curl output for " + rateName + ":");
            System.out.println(output.toString());

            // Assert that the curl command executed successfully and output is not empty
            assertTrue(exitCode == 0, "Curl command failed for " + rateName + " with exit code " + exitCode);
            assertFalse(output.toString().isEmpty(), "Curl output is empty for " + rateName);
        }

        // There is no system shutdown or termination code here.
        // After the test completes, the service continues running.
    }
} 