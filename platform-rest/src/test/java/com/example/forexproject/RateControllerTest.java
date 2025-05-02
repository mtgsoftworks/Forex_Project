package com.example.forexproject;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RateController.class)
public class RateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testGetRate() throws Exception {
        var result = mockMvc.perform(get("/api/rates/PF2_USDTRY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rateName").value("PF2_USDTRY"))
                .andReturn();
        String responseContent = result.getResponse().getContentAsString();
        System.out.println("RateControllerTest response: " + responseContent);
    }
}