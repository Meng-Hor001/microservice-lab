# Payment unavailable test

Date: 2026-10-25 (UTC)

Request: `GET http://localhost:9292/api/customers`

Payment was already stopped when testing began: port 8484 refused connections and Eureka returned HTTP 404 for `/eureka/apps/PAYMENT-SERVICE`.

## Payment unavailable — 09:07:38 UTC

Actual status: **500 Internal Server Error**

Content-Type: `application/problem+json`

```json
{"detail":"An unexpected error occurred. Please try again later.","instance":"/api/customers","status":500,"title":"Internal Server Error"}
```

## Payment restarted — 09:08:05 UTC

Started payment with `./gradlew :microservices:payment-service:payment-service-main:bootRun --console=plain`.
Payment started on port 8484 and registered with Eureka (registration status 204).

Repeated the same customer request: **200 OK**, `application/json`, six customers with credit balances. Payment was left running.

The outage response matches the generic exception handler in `RestAdvisor`. `PaymentCreditClient` maps `RestClientException` to 503, but other exception types can reach this generic 500 handler. The exact exception requires the customer-service error log; it was not captured in this test.
