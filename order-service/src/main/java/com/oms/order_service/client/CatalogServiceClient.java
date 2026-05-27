package com.oms.order_service.client;

import com.oms.order_service.dto.CustomerDetailsDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Component
@Slf4j
public class CatalogServiceClient {

    private final RestClient restClient;

    // No parameters needed! We just instantiate it directly using standard Java.
    public CatalogServiceClient() {
        // Create the client directly and set the base URL
        this.restClient = RestClient.create("http://localhost:8082");
    }

    @CircuitBreaker(name = "catalogService", fallbackMethod = "getCustomerDetailsFallback")
    public CustomerDetailsDto getCustomerDetails(String customerCode) {
        log.info("Fetching customer details synchronously for customer: {}", customerCode);

        return restClient.get()
                // Because we set the base URL above, we only need the endpoint path here
                .uri("/api/v1/customers/{code}", customerCode)
                .retrieve()
                .body(CustomerDetailsDto.class);
    }

    public BigDecimal getProductPrice(String pricebookCode, String productCode) {
        log.info("Fetching price synchronously from Catalog for Product: {} in Pricebook: {}", productCode, pricebookCode);

        try {
            // GET request to: http://localhost:8082/api/v1/prices/{pricebookCode}/{productCode}
            return restClient.get()
                    .uri("/api/v1/prices/{pricebook}/{product}", pricebookCode, productCode)
                    .retrieve()
                    .body(BigDecimal.class);
        } catch (Exception e) {
            log.error("Failed to fetch price for product: {}. Defaulting to 0.00", productCode, e);
            return BigDecimal.ZERO;
        }
    }

    // This executes IMMEDIATELY if the Catalog Service is down or the circuit is open
    public CustomerDetailsDto getCustomerDetailsFallback(String customerCode, Throwable throwable) {
        log.error("Circuit Breaker Tripped! Catalog Service is unresponsive. Error: {}", throwable.getMessage());

        // Instead of crashing, we gracefully reject the order placement
        throw new IllegalStateException("We are currently experiencing heavy load. Order placement for " + customerCode + " has been temporarily paused. Please try again in 10 seconds.");
    }
}