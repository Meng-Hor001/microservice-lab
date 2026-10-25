package kh.edu.istad.platform.customer.restapi.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class PaymentCreditClient {
    private final RestClient client;

    public PaymentCreditClient() {

        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));

        client = RestClient.builder()
                .baseUrl("http://localhost:8484")
                .requestFactory(factory)
                .build();
    }

    public Map<UUID, BigDecimal> getBalances(List<UUID> customerIds) {
        if (customerIds.isEmpty()) {
            return Map.of();
        }

        try {
            Map<UUID, BigDecimal> balances = client.get()
                    .uri(builder -> builder
                            .path("/api/customers/credit-balances")
                            .queryParam("customerIds", customerIds.toArray())
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<
                                                Map<UUID, BigDecimal>>() {
                    });

            if (balances == null
                    || customerIds.stream().anyMatch(id -> balances.get(id) == null)) {
                throw new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Payment service returned incomplete credit balances"
                );
            }

            return balances;

        } catch (RestClientException exception) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Customer credit balances are temporarily unavailable",
                    exception
            );
        }
    }
}
