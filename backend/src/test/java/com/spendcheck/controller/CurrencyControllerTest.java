package com.spendcheck.controller;

import com.spendcheck.model.User;
import com.spendcheck.repository.UserRepository;
import com.spendcheck.service.CurrencyService;
import com.spendcheck.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CurrencyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CurrencyService currencyService;

    private String token;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        User user = userRepository.save(new User("Currency User", "currency@example.com", passwordEncoder.encode("password123")));
        token = "Bearer " + jwtService.generateToken(user);
        currencyService.clearCache();
    }

    @Test
    void convert_withoutAuth_returnsUnauthorized401() throws Exception {
        mockMvc.perform(get("/api/v1/currency/convert?from=USD&to=INR&amount=100"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void convert_sameCurrency_returnsOneToOneRate() throws Exception {
        mockMvc.perform(get("/api/v1/currency/convert?from=USD&to=USD&amount=100.50")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.from").value("USD"))
                .andExpect(jsonPath("$.data.to").value("USD"))
                .andExpect(jsonPath("$.data.amount").value(100.50))
                .andExpect(jsonPath("$.data.convertedAmount").value(100.50))
                .andExpect(jsonPath("$.data.rate").value(1.0000));
    }

    @Test
    void convert_invalidAmount_returnsBadRequest400() throws Exception {
        mockMvc.perform(get("/api/v1/currency/convert?from=USD&to=INR&amount=-50")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Amount must be greater than zero"));
    }
}

