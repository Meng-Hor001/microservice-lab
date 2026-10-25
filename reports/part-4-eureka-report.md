# Part 4 Eureka Health Tracking and Service Discovery

Student: [Your name]  
Class: [Your class]  
Date: [Experiment date]

## 1 Objective

This experiment investigates how Eureka tracks service registration, application health, graceful shutdown, and unexpected unavailability. It also compares client-side and server-side discovery and load balancing. The key question is how long discovery information takes to reflect a change in the actual service.

## 2 Project setup

| Component | Address | Role |
|---|---|---|
| Discovery server | http://localhost:8761 | Eureka registry and dashboard |
| Payment service | http://localhost:8484 | Service being tested |
| Customer service | http://localhost:9292 | Optional caller for communication tests |
| Payment health endpoint | http://localhost:8484/actuator/health | Application health evidence |

The payment module already includes the Eureka client and Actuator dependencies. Its configuration enables `eureka.client.healthcheck.enabled`. The discovery server currently sets `eureka.server.enable-self-preservation: false`. The customer client uses `http://payment-service` through a load-balanced `RestClient.Builder`.

Both customer and payment currently expect PostgreSQL at `localhost:5432/microservice_db`. Ensure this database and the configured credentials work before testing. The deployment Compose file's order database on port 5433 does not automatically satisfy this requirement. Do not include database passwords in screenshots.

### Start the applications

Run these commands from the repository root in separate terminals. Start the database first, then Eureka, then payment. Start customer only if testing service-to-service calls.

```bash
./gradlew -p microservices/discovery-server bootRun
```

```bash
./gradlew -p microservices/payment-service :payment-service-main:bootRun
```

```bash
./gradlew -p microservices/customer-service :customer-service-main:bootRun
```

Wait for successful application startup logs. Open the dashboard and check health:

```bash
curl -i http://localhost:8484/actuator/health
curl -sS -H 'Accept: application/json' http://localhost:8761/eureka/apps/PAYMENT-SERVICE
```

A build error or database connection error must be resolved before collecting Eureka results. Record your actual Spring Boot and Spring Cloud versions with the experiment.

## 3 Relevant configuration

Eureka uses client heartbeats to track leases. Application health is a separate signal: enabling health propagation lets a running client report an unhealthy application through Actuator. Merely publishing a health URL does not mean the Eureka server actively polls it. See [Spring Cloud Netflix documentation](https://docs.spring.io/spring-cloud-netflix/reference/spring-cloud-netflix.html).

| Property | Purpose | Baseline and test value |
|---|---|---|
| `eureka.client.healthcheck.enabled` | Propagate application health | Payment already uses true; compare false |
| `eureka.instance.lease-renewal-interval-in-seconds` | Heartbeat interval | Default 30; optional lab value 5 |
| `eureka.instance.lease-expiration-duration-in-seconds` | Lease expiration interval | Default 90; optional lab value 15 |
| `eureka.server.eviction-interval-timer-in-ms` | Server eviction scan interval | Default 60000; optional lab value 5000 |
| `eureka.server.enable-self-preservation` | Protect registrations during large renewal drops | Project uses false; compare true |
| `eureka.client.registry-fetch-interval-seconds` | Caller registry refresh interval | Default 30; optional lab value 5 |

Defaults are documented in the [configuration reference](https://docs.spring.io/spring-cloud-netflix/reference/configprops.html). Keep expiration longer than renewal. The intervals are scheduling inputs, not a guarantee that an entry disappears at an exact second. Eviction limits, server response caching, client caching, and scheduling can add delay.

First run with the existing settings. Then, for a faster local comparison, merge these fields into the existing payment YAML. Do not add duplicate top-level `eureka` keys or replace unrelated settings.

```yaml
eureka:
  client:
    healthcheck:
      enabled: true
  instance:
    lease-renewal-interval-in-seconds: 5
    lease-expiration-duration-in-seconds: 15
```

Merge into the discovery server YAML:

```yaml
eureka:
  server:
    enable-self-preservation: false
    eviction-interval-timer-in-ms: 5000
```

Optionally set `eureka.client.registry-fetch-interval-seconds: 5` in the customer service. Restart each changed application. Use the short intervals only for this lab and restore the saved configuration afterward.

## 4 Experiment procedure and screenshot evidence

For every test, record the action time, the time the dashboard changes, and the actual endpoint result. Refresh the dashboard periodically. Record the elapsed time as the difference between the two timestamps. Expected outcomes below are hypotheses to compare with your measurements.

### Experiment A Service startup

1. Start Eureka with payment stopped. Capture the registry before registration.
2. Start payment and note its startup time.
3. Refresh the dashboard until `PAYMENT-SERVICE` appears as UP.
4. Request `/actuator/health` and capture its response.

Expected: payment registers and reports UP once healthy. Registration and visible registry updates may take time.

**Figure 1:** [Insert dashboard screenshot showing payment UP and instance port.]  
**Figure 2:** [Insert health response screenshot showing request URL, HTTP status, and body.]

Actual observation: Payment started at [time], appeared as [status] at [time], and returned [HTTP status and body]. Registration visibility delay was [seconds].

### Experiment B Graceful shutdown

1. Confirm payment is UP.
2. Press Ctrl+C in its `bootRun` terminal and record the time.
3. Check the shutdown logs for deregistration or cancellation messages.
4. Refresh the registry and request the health endpoint again.

Expected: a clean shutdown normally sends a cancellation to Eureka. The instance disappears after registry updates; the endpoint becomes unreachable. A visible DOWN intermediate state is not required. If cancellation is absent, record that and compare with lease expiration behavior.

**Figure 3:** [Insert shutdown logs and dashboard after removal.]

Actual observation: Cancellation [was or was not] visible in logs. The registry changed after [seconds]. The health request returned [result].

### Experiment C Unexpected unavailability

Use a suspended payment process to simulate a hung service without allowing graceful deregistration. This tests missing heartbeats; it does not simulate every kind of network partition.

1. Restart payment and wait for UP.
2. Find its application JVM using `jps -lv`. Identify `kh.edu.istad.platform.payment.PaymentApplication`. Do not select a Gradle daemon, the customer JVM, or the discovery server.
3. Substitute the verified numeric PID for `PAYMENT_PID` below:

```bash
kill -STOP PAYMENT_PID
date -Is
curl --max-time 5 -i http://localhost:8484/actuator/health
```

4. Capture the initial timeout and registry status. Continue observing until the entry disappears, or until your stated observation window ends. Use at least several minutes for default settings.
5. Always resume the same process afterward:

```bash
kill -CONT PAYMENT_PID
```

Expected: requests time out while the JVM is suspended. Eureka may temporarily continue showing UP because the last registration remains valid. With self-preservation disabled, expired leases become eligible for eviction. Removal need not occur exactly 90 seconds after suspension, or exactly 15 seconds with the faster settings. After resuming, observe whether the client renews or re-registers.

**Figure 4:** [Insert timed-out request beside the initially stale UP entry.]  
**Figure 5:** [Insert registry after eventual removal, with timestamps.]

Actual observation: Suspended at [time]. Initial registry status was [status]. Removal occurred at [time] after [seconds], or was not seen within [duration]. After resuming, [result].

### Experiment D Application unhealthy while the JVM is running

Use a temporary health indicator so that shared database services need not be stopped. Add the following class under `microservices/payment-service/payment-service-main/src/main/java/kh/edu/istad/platform/payment/`. It uses Actuator already present in the payment module.

```java
package kh.edu.istad.platform.payment;

import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class LabHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        return Files.exists(Path.of("/tmp/payment-unhealthy"))
                ? Health.down().build()
                : Health.up().build();
    }
}
```

1. Restart payment and verify that its health is initially UP.
2. Run `touch /tmp/payment-unhealthy`.
3. Request `/actuator/health`, then watch Eureka until the status changes.
4. Remove your test marker with `rm /tmp/payment-unhealthy` and observe recovery.
5. Change payment's `eureka.client.healthcheck.enabled` to false, restart, and repeat. Restore true afterward.

Expected with health propagation enabled: Actuator returns DOWN, normally with HTTP 503, and Eureka eventually reflects DOWN while the JVM remains alive. Removing the marker allows recovery if all other health contributors are UP. With propagation disabled, Actuator can report DOWN while Eureka continues to show UP based on the running client's registration and heartbeats.

**Figure 6:** [Insert Actuator DOWN and corresponding Eureka status.]  
**Figure 7:** [Insert the healthcheck-disabled comparison and recovery evidence.]

Actual observation: With propagation enabled, [result and delay]. With propagation disabled, [result]. Recovery took [seconds]. Remove the temporary Java class after finishing the lab.

### Experiment E Self preservation comparison

1. Repeat Experiment C with `enable-self-preservation: false` and record removal behavior.
2. Resume payment. Set the server property to true and restart Eureka.
3. Wait for clients to re-register and establish normal renewals before suspending payment again.
4. Record the dashboard's renewal threshold, actual renewals, warning banner, and entry retention during a defined observation window, such as five minutes.
5. Resume payment and restore your baseline settings.

Expected: when renewals fall below the server's threshold, self-preservation can suppress expiration eviction and retain stale entries. A small lab or recently restarted server may not behave identically to a large stable deployment; do not claim activation unless the dashboard or logs support it. A graceful cancellation is different from expiration eviction.

**Figure 8:** [Insert self-preservation warning and retained entry, or evidence that it did not activate.]

Actual observation: Threshold [value], renewals [value], warning [text or absent], observation window [duration], registry result [result].

Self-preservation protects against removing many reachable services when the registry loses network contact with them. It trades faster removal for protection during suspected connectivity failures. See [Netflix's explanation](https://github.com/Netflix/eureka/wiki/Server-Self-Preservation-Mode).

## 5 Results table

Complete this table using the experiments and screenshots, not the expected outcomes.

| Test | Observed endpoint result | Observed Eureka status | Measured delay | Evidence |
|---|---|---|---|---|
| Startup | [Record] | [Record] | [Seconds] | Figures 1 and 2 |
| Graceful shutdown | [Record] | [Record] | [Seconds] | Figure 3 |
| Suspended process | [Record] | [Initial and final] | [Seconds or window] | Figures 4 and 5 |
| Health reporting enabled | [Record] | [Record] | [Seconds] | Figure 6 |
| Health reporting disabled | [Record] | [Record] | [Seconds or window] | Figure 7 |
| Self preservation enabled | [Record] | [Record] | [Seconds or window] | Figure 8 |

## 6 Client side and server side discovery and load balancing

Service discovery determines which addresses belong to a service. Load balancing chooses an instance to receive a request. A registry supplies discovery information; it does not automatically proxy business traffic.

### Client side approach

The caller obtains instance addresses from a registry and selects a destination using a client-side load balancer. In this project, the customer service's load-balanced REST client resolves `payment-service` using Eureka information and sends the request directly to the selected payment instance. Eureka does not forward that request.

```text
Customer -- registry lookup and refresh --> Eureka
Customer -- selected instance request --> Payment instance
```

This avoids a separate routing hop, but each caller needs discovery and balancing support. Cached addresses can become stale, so calls may still fail during changes. See the [client-side discovery pattern](https://microservices.io/patterns/client-side-discovery.html).

### Server side approach

The caller sends its request to a stable router or load balancer. That component discovers available instances, selects one, and forwards the request. An application load balancer or a gateway integrated with service discovery can implement this pattern.

```text
Caller --> Router or load balancer --> Selected payment instance
                   |
                   +--> Registry or backend membership information
```

This keeps instance selection out of application callers and centralizes routing policy. It adds routing infrastructure that needs adequate capacity and availability, and typically adds a network hop. See the [server-side discovery pattern](https://microservices.io/patterns/server-side-discovery.html).

| Aspect | Client side | Server side |
|---|---|---|
| Finds instances | Calling application library | Router or infrastructure |
| Chooses destination | Client load balancer | Router load balancer |
| Business request path | Caller directly to instance | Caller through router to instance |
| Caller requirement | Discovery and balancing integration | Stable router address |
| Main operational concern | Consistent client behavior and stale caches | Router capacity and availability |

A gateway can itself use a client-side load-balancing library for its backend calls while presenting server-side discovery to external callers. The distinction depends on whose request path is being described.

### Optional load balancing demonstration

Start a second payment instance on a different port:

```bash
./gradlew -p microservices/payment-service :payment-service-main:bootRun --args='--server.port=8485'
```

The existing instance ID includes the port, so the dashboard should distinguish the two instances. Send repeated customer requests that contain actual customer IDs with matching payment records. An empty customer list bypasses the payment call and proves nothing about balancing. Use payment request logs with instance ports or another explicit instance identifier to show which instance handled each request. Two UP entries demonstrate registration, but do not by themselves demonstrate traffic distribution. Do not claim exact alternation or successful failover without evidence.

## 7 Discussion and conclusion

Complete after testing:

“The startup test showed [observation]. A graceful stop changed the registry after [time], whereas unexpected unavailability changed it after [time or observation window]. This difference was consistent with [cancellation versus missed-heartbeat explanation supported by logs]. Enabling application health reporting produced [comparison]. With self-preservation enabled, [observed behavior and evidence].”

“The project uses client-side discovery for customer-to-payment calls because the customer selects and contacts a discovered payment instance. A server-side alternative would route requests through a component that selects instances on the caller's behalf. The experiments demonstrate [supported conclusion]. Remaining limitations include [uncompleted tests or evidence gaps].”

## 8 Submission checklist

- [ ] Replace student details and all observation placeholders.
- [ ] Record configuration values used for each run.
- [ ] Insert actual screenshots with figure captions and timestamps.
- [ ] Compare startup, graceful shutdown, and unexpected unavailability.
- [ ] Compare health reporting enabled and disabled.
- [ ] Explain self-preservation using observed evidence.
- [ ] Keep the research links with the report.
- [ ] Resume any suspended process and remove the temporary health indicator and marker.
- [ ] Restore baseline configuration after the experiment.
- [ ] Paste into Google Docs, format headings and code, and review tables and screenshots before submission.
