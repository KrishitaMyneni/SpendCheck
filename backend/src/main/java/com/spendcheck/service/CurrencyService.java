package com.spendcheck.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.spendcheck.dto.CurrencyConversionResponse;
import com.spendcheck.exception.CurrencyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CurrencyService {

    private static final Logger log = LoggerFactory.getLogger(CurrencyService.class);
    private static final Duration CACHE_DURATION = Duration.ofHours(1);
    private static final String PRIMARY_API_URL = "https://api.exchangerate-api.com/v4/latest/";
    private static final String FALLBACK_API_URL = "https://open.er-api.com/v6/latest/";

    private final RestTemplate restTemplate;
    private final Map<String, CachedRates> rateCache = new ConcurrentHashMap<>();

    public CurrencyService() {
        this(new RestTemplateBuilder());
    }

    public CurrencyService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(5))
                .build();
    }

    // Constructor for testing with custom RestTemplate
    public CurrencyService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public CurrencyConversionResponse convert(String from, String to, BigDecimal amount) {
        if (from == null || from.trim().isBlank()) {
            throw new IllegalArgumentException("Source currency ('from') is required");
        }
        if (to == null || to.trim().isBlank()) {
            throw new IllegalArgumentException("Target currency ('to') is required");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        String base = from.trim().toUpperCase();
        String target = to.trim().toUpperCase();

        if (base.equals(target)) {
            return new CurrencyConversionResponse(
                    base,
                    target,
                    amount,
                    BigDecimal.ONE.setScale(4, RoundingMode.HALF_UP),
                    amount.setScale(2, RoundingMode.HALF_UP),
                    true
            );
        }

        CachedRates cached = rateCache.get(base);
        boolean isCached = cached != null && !cached.isExpired();
        Map<String, BigDecimal> rates;

        if (isCached) {
            rates = cached.rates();
        } else {
            rates = fetchRatesFromApi(base);
            rateCache.put(base, new CachedRates(rates, Instant.now().plus(CACHE_DURATION)));
        }

        BigDecimal rate = rates.get(target);
        if (rate == null) {
            throw new IllegalArgumentException("Target currency '" + target + "' is not supported");
        }

        BigDecimal convertedAmount = amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);

        return new CurrencyConversionResponse(
                base,
                target,
                amount,
                rate.setScale(4, RoundingMode.HALF_UP),
                convertedAmount,
                isCached
        );
    }

    private Map<String, BigDecimal> fetchRatesFromApi(String baseCurrency) {
        // Try primary provider
        try {
            return callApi(PRIMARY_API_URL + baseCurrency);
        } catch (Exception e1) {
            log.warn("Primary exchange rate API failed for {}: {}. Attempting fallback...", baseCurrency, e1.getMessage());
            try {
                return callApi(FALLBACK_API_URL + baseCurrency);
            } catch (Exception e2) {
                log.error("Fallback exchange rate API also failed for {}: {}", baseCurrency, e2.getMessage());
                throw new CurrencyException("Unable to fetch real-time exchange rates. Please check external service availability.", e2);
            }
        }
    }

    private Map<String, BigDecimal> callApi(String url) {
        try {
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new CurrencyException("Exchange rate provider responded with status " + response.getStatusCode());
            }

            JsonNode root = response.getBody();
            JsonNode ratesNode = root.path("rates");
            if (ratesNode.isMissingNode() || !ratesNode.isObject()) {
                throw new CurrencyException("Invalid response format from exchange rate provider");
            }

            Map<String, BigDecimal> rateMap = new ConcurrentHashMap<>();
            Iterator<Map.Entry<String, JsonNode>> fields = ratesNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                if (field.getValue().isNumber()) {
                    rateMap.put(field.getKey().toUpperCase(), field.getValue().decimalValue());
                }
            }

            if (rateMap.isEmpty()) {
                throw new CurrencyException("No exchange rates returned for requested currency");
            }

            return rateMap;
        } catch (RestClientException e) {
            throw new CurrencyException("Exchange rate network error: " + e.getMessage(), e);
        }
    }

    // Helper for testing cache expiration
    public void clearCache() {
        this.rateCache.clear();
    }

    private record CachedRates(Map<String, BigDecimal> rates, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}
