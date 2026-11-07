package com.spendcheck.controller;

import com.spendcheck.dto.ApiResponse;
import com.spendcheck.dto.CurrencyConversionResponse;
import com.spendcheck.service.CurrencyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/currency")
public class CurrencyController {

    private final CurrencyService currencyService;

    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @GetMapping("/convert")
    public ResponseEntity<ApiResponse<CurrencyConversionResponse>> convert(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam BigDecimal amount
    ) {
        CurrencyConversionResponse response = currencyService.convert(from, to, amount);
        return ResponseEntity.ok(ApiResponse.ok("Currency converted successfully", response));
    }
}

