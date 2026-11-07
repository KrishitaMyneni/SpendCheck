package com.spendcheck.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spendcheck.dto.ExpenseRequest;
import com.spendcheck.model.Category;
import com.spendcheck.model.User;
import com.spendcheck.repository.ExpenseRepository;
import com.spendcheck.repository.UserRepository;
import com.spendcheck.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User userA;
    private User userB;
    private String tokenA;
    private String tokenB;

    @BeforeEach
    void setUp() {
        expenseRepository.deleteAll();
        userRepository.deleteAll();

        userA = userRepository.save(new User("User A", "userA@example.com", passwordEncoder.encode("password123")));
        userB = userRepository.save(new User("User B", "userB@example.com", passwordEncoder.encode("password123")));

        tokenA = "Bearer " + jwtService.generateToken(userA);
        tokenB = "Bearer " + jwtService.generateToken(userB);
    }

    @Test
    void createExpense_success_returnsCreated() throws Exception {
        ExpenseRequest request = new ExpenseRequest(
                "Lunch with team",
                new BigDecimal("450.50"),
                Category.FOOD,
                LocalDate.now(),
                "Office lunch"
        );

        mockMvc.perform(post("/api/v1/expenses")
                        .header(HttpHeaders.AUTHORIZATION, tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.title").value("Lunch with team"))
                .andExpect(jsonPath("$.data.amount").value(450.50))
                .andExpect(jsonPath("$.data.category").value("FOOD"))
                .andExpect(jsonPath("$.data.categoryDisplayName").value("Food"));
    }

    @Test
    void getExpenses_withFilters_returnsMatchingRecordsOnly() throws Exception {
        LocalDate today = LocalDate.now();
        ExpenseRequest foodToday = new ExpenseRequest("Burger", new BigDecimal("150.00"), Category.FOOD, today, null);
        ExpenseRequest foodYesterday = new ExpenseRequest("Pizza", new BigDecimal("300.00"), Category.FOOD, today.minusDays(1), null);
        ExpenseRequest travelToday = new ExpenseRequest("Metro", new BigDecimal("50.00"), Category.TRAVEL, today, null);

        mockMvc.perform(post("/api/v1/expenses").header(HttpHeaders.AUTHORIZATION, tokenA)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(foodToday))).andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/expenses").header(HttpHeaders.AUTHORIZATION, tokenA)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(foodYesterday))).andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/expenses").header(HttpHeaders.AUTHORIZATION, tokenA)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(travelToday))).andExpect(status().isCreated());

        // Filter by category FOOD
        mockMvc.perform(get("/api/v1/expenses?category=FOOD")
                        .header(HttpHeaders.AUTHORIZATION, tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));

        // Filter by date range (today only)
        mockMvc.perform(get("/api/v1/expenses?startDate=" + today + "&endDate=" + today)
                        .header(HttpHeaders.AUTHORIZATION, tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    void userIsolation_userBCannotAccessUserAExpense() throws Exception {
        ExpenseRequest request = new ExpenseRequest("Private Expense", new BigDecimal("1000.00"), Category.ESSENTIALS, LocalDate.now(), null);

        String createResponse = mockMvc.perform(post("/api/v1/expenses")
                        .header(HttpHeaders.AUTHORIZATION, tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long expenseId = objectMapper.readTree(createResponse).path("data").path("id").asLong();

        // User B tries to GET User A's expense -> 404
        mockMvc.perform(get("/api/v1/expenses/" + expenseId)
                        .header(HttpHeaders.AUTHORIZATION, tokenB))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        // User B tries to UPDATE User A's expense -> 404
        ExpenseRequest updateReq = new ExpenseRequest("Hacked Title", new BigDecimal("1.00"), Category.OTHER, LocalDate.now(), null);
        mockMvc.perform(put("/api/v1/expenses/" + expenseId)
                        .header(HttpHeaders.AUTHORIZATION, tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isNotFound());

        // User B tries to DELETE User A's expense -> 404
        mockMvc.perform(delete("/api/v1/expenses/" + expenseId)
                        .header(HttpHeaders.AUTHORIZATION, tokenB))
                .andExpect(status().isNotFound());

        // User A CAN delete their own expense -> 200
        mockMvc.perform(delete("/api/v1/expenses/" + expenseId)
                        .header(HttpHeaders.AUTHORIZATION, tokenA))
                .andExpect(status().isOk());
    }

    @Test
    void categorySummary_returnsAggregatesAndPercentages() throws Exception {
        ExpenseRequest exp1 = new ExpenseRequest("Dinner", new BigDecimal("600.00"), Category.FOOD, LocalDate.now(), null);
        ExpenseRequest exp2 = new ExpenseRequest("Bus", new BigDecimal("400.00"), Category.TRAVEL, LocalDate.now(), null);

        mockMvc.perform(post("/api/v1/expenses").header(HttpHeaders.AUTHORIZATION, tokenA)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(exp1))).andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/expenses").header(HttpHeaders.AUTHORIZATION, tokenA)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(exp2))).andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/expenses/summary/categories")
                        .header(HttpHeaders.AUTHORIZATION, tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].category").value("FOOD"))
                .andExpect(jsonPath("$.data[0].totalAmount").value(600.00))
                .andExpect(jsonPath("$.data[0].percentage").value(60.00))
                .andExpect(jsonPath("$.data[1].category").value("TRAVEL"))
                .andExpect(jsonPath("$.data[1].totalAmount").value(400.00))
                .andExpect(jsonPath("$.data[1].percentage").value(40.00));
    }

    @Test
    void monthlySummary_returnsAggregates() throws Exception {
        LocalDate date = LocalDate.of(2026, 5, 10);
        ExpenseRequest exp = new ExpenseRequest("Books", new BigDecimal("500.00"), Category.EDUCATION, date, null);

        mockMvc.perform(post("/api/v1/expenses").header(HttpHeaders.AUTHORIZATION, tokenA)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(exp))).andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/expenses/summary/monthly?year=2026&month=5")
                        .header(HttpHeaders.AUTHORIZATION, tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].year").value(2026))
                .andExpect(jsonPath("$.data[0].month").value(5))
                .andExpect(jsonPath("$.data[0].monthName").value("May"))
                .andExpect(jsonPath("$.data[0].totalAmount").value(500.00));
    }
}

