# Part 3 Eureka Service Discovery Integration Report

**Project:** Microservice Lab  
**Student:** [Enter your name]  
**Class:** [Enter your class]  
**Date:** [Enter submission date]

## 1 Introduction

This part of the project introduces Eureka service discovery to reduce dependence on fixed service addresses. The objectives are to replace hard-coded service URLs, verify communication between services, and compare the benefits and limitations of the two approaches.

The integration is currently partial. The discovery server and Eureka client configuration for the customer and payment services are present. However, the customer payment client contains a URL error, and the business service still calls the order service through a fixed address. Runtime testing and screenshots remain necessary before claiming completion.

## 2 Current Implementation

### 2.1 Discovery server

The discovery-server module includes the Eureka server dependency and uses `@EnableEurekaServer`. Its configuration disables self-registration and registry fetching. The configured Eureka address is `http://localhost:8761/eureka/`.

### 2.2 Customer and payment services

Both services include the Eureka client dependency and configure the same registry address. Their application names and configured ports are:

| Service name | Configured port | Eureka client configuration |
|---|---|---|
| customer-service | 9292 | Present |
| payment-service | 8484 | Present |

The customer service defines an `@LoadBalanced RestClient.Builder` and injects it into `PaymentCreditClient`. This client requests payment balances through `/api/customers/credit-balances`.

The intended communication flow is:

```text
Client -> Customer service -> Payment service -> Customer service -> Client
                              ^
                Target resolved through Eureka
```

The current base URL is incorrect:

```java
.baseUrl("http:payment-service")
```

It must be corrected to:

```java
.baseUrl("http://payment-service")
```

This is a required correction, not a change already completed as part of this report.

### 2.3 Remaining fixed service address

The business service configuration still contains:

```yaml
order-service:
  base-url: ${ORDER_SERVICE_BASE_URL:http://localhost:8383}
```

`HttpOrderApprovalPublisher` creates a regular `WebClient` using this value. Although an environment variable can override the address, the call does not currently use Eureka discovery. To migrate this communication path, the business and order services need Eureka client integration, and the publisher needs a load-balanced client targeting the registered order service name.

The registry URL itself is a bootstrap address. Configuring the Eureka address is different from hard-coding the destination of a business service call.

## 3 Testing and Results

### 3.1 Current verification status

Source inspection confirms the configuration described above. A runtime availability check was attempted on 25 October 2026. Nothing was listening on the Eureka, customer, or payment ports, so functional service-to-service communication could not be tested in this run. The observed results are separated from tests that remain unrun below.

| Test | Procedure | Expected outcome | Actual result |
|---|---|---|---|
| Eureka availability | GET http://localhost:8761/ | Dashboard or Eureka response is reachable | Connection refused; no process was listening | Not run |
| Customer API availability | GET http://localhost:9292/api/customers | Customer API returns an HTTP response | Connection refused; no process was listening | Not run |
| Payment API availability | Request the payment balance endpoint on port 8484 | Payment API returns an HTTP response | Connection refused; no process was listening | Not run |
| Customer to payment communication | Start services and databases, then request customers with payment data | Customer response contains balances retrieved from payment | Not run; services were unavailable | Not run |
| Payment port change | Restart payment on another port and repeat the customer request without changing caller configuration | Communication succeeds through the registered service name | Not run | Not run |
| Payment unavailable behavior | Stop payment and repeat a customer request with a nonempty ID list | Customer API returns a handled error | Not run; customer service was unavailable before the request | Not run |
| Business to order communication | After migrating this path, submit a valid approval for an existing order | Approval reaches the order service through discovery | Not run; fixed-address call remains | Not run |

For the customer-to-payment test, use a nonempty customer list. `PaymentCreditClient` returns immediately when there are no customer IDs, so an empty response does not prove service-to-service communication. Ensure payment records exist for the tested customers: missing balances are treated as an unavailable or incomplete response.

### 3.2 Screenshot evidence to include

1. **Figure 1 Service registration:** Capture the Eureka dashboard showing the required services and their UP status.
2. **Figure 2 Successful communication:** Capture the customer request URL, HTTP method, response status, and returned customer balances. Include payment logs or matching payment data to support the service-to-service result.
3. **Figure 3 Discovery after a port change:** Capture the payment service at its new registered port and the successful repeated customer request.
4. **Figure 4 Unavailable service:** Capture the response returned when the payment service is stopped.
5. **Figure 5 Business to order communication:** If all service calls are in scope, capture successful approval delivery after migrating this path.

Insert the actual screenshots below the corresponding captions before submission. For each screenshot, describe the input, observed response, and whether it matched the expected result.

### 3.3 Results summary to complete

Availability checks attempted: **3**  
Services reachable: **0**  
Functional communication tests completed: **0**  
Observed result: Requests to ports 8761, 9292, and 8484 failed with connection refused because the services were not running. This does not establish whether the configured service-to-service call succeeds or fails once the services are started.

## 4 Comparison of Approaches

| Aspect | Fixed service addresses | Eureka with a load-balanced client |
|---|---|---|
| Destination | Caller uses a configured host and port | Caller uses a registered service name |
| Address changes | Caller configuration must be updated when the destination changes | Caller can resolve the new address after registration and registry refresh |
| Multiple instances | Requires additional routing or instance-selection configuration | The load-balanced client can select from discovered instances |
| Local setup | Fewer components are required | A registry and client configuration are required |
| Failure handling | Requests fail when the configured destination is unavailable | Discovery helps locate instances, but requests can still fail |

The main expected improvement is reduced coupling between callers and service locations. A caller can continue using the same service name when an instance changes port or host. Discovery also supports the use of multiple registered instances together with client-side load balancing. These benefits still require runtime verification in this project; no performance improvement has been measured.

## 5 Remaining Limitations

- **Incomplete migration:** Business-to-order communication still uses a fixed address.
- **Client URL defect:** The payment service URL must be corrected before verifying the customer call.
- **Registry update delay:** Instance registration, removal, and cached discovery information may take time to reflect changes.
- **Registry operations:** Eureka adds a component to configure and maintain. Registry outages can affect registration and discovery refresh; cached instances may permit some existing calls to continue temporarily.
- **Communication reliability:** Discovery does not by itself guarantee successful calls or provide application-level retries, circuit breakers, or fallbacks.
- **Business data consistency:** Locating a payment service does not guarantee that it contains balances for every customer or that updates across services remain consistent.
- **Missing runtime evidence:** The test results and screenshots must be added before the integration can be reported as verified.

## 6 Conclusion

The project contains the foundation for Eureka-based service discovery through its discovery server and the customer and payment client configurations. The customer service also contains a load-balanced REST client intended to call payment by service name.

Part 3 is not yet fully complete. The payment URL needs correction, the remaining business-to-order call needs migration if all service communication is in scope, and runtime tests must demonstrate successful discovery-based communication. In the availability check on 25 October 2026, the Eureka, customer, and payment services were not running, so communication results could not be collected. Start the services and databases, rerun the functional tests, and add screenshots before submitting.

## 7 Completion Checklist

- [ ] Correct the payment service URL to `http://payment-service`.
- [ ] Confirm the required services register successfully with Eureka.
- [ ] Migrate business-to-order communication if included in the assignment scope.
- [ ] Verify a customer request with real payment balance data.
- [ ] Verify communication after changing the payment port.
- [ ] Record behavior when the payment service is unavailable.
- [ ] Add screenshots and actual test results.
- [ ] Update the conclusion to reflect the final verified state.

## 8 Project Files Reviewed

- `microservices/discovery-server/build.gradle`
- `microservices/discovery-server/src/main/java/kh/edu/istad/platform/discovery/DiscoveryApplicationService.java`
- `microservices/discovery-server/src/main/resources/application.yaml`
- `microservices/customer-service/customer-service-main/build.gradle`
- `microservices/customer-service/customer-service-main/src/main/resources/application.yaml`
- `microservices/customer-service/customer-service-restapi/src/main/java/kh/edu/istad/platform/customer/restapi/client/PaymentCreditClient.java`
- `microservices/customer-service/customer-service-restapi/src/main/java/kh/edu/istad/platform/customer/restapi/config/RestClientConfig.java`
- `microservices/payment-service/payment-service-main/build.gradle`
- `microservices/payment-service/payment-service-main/src/main/resources/application.yaml`
- `microservices/business-service/business-service-main/src/main/resources/application.yaml`
- `microservices/business-service/business-service-restapi/src/main/java/kh/edu/istad/platform/business/restapi/client/HttpOrderApprovalPublisher.java`
