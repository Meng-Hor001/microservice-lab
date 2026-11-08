package kh.edu.istad.platform.order;

import kh.edu.istad.platform.order.config.ServicePropConfig;
import kh.edu.istad.platform.order.domain.dto.QueryCustomerResult;
import kh.edu.istad.platform.order.domain.port.out.client.CustomerServiceClient;
import kh.edu.istad.common.domain.valueobject.CustomerId;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@RefreshScope
@RestController
@RequiredArgsConstructor
public class WebFluxController {

    private final CustomerServiceClient customerServiceClient;

    @GetMapping("customers")
    public Flux<QueryCustomerResult> getCustomers(){
        return customerServiceClient.getCustomerNames();
    }

    @GetMapping("customers/{customerId}")
    public Mono<QueryCustomerResult> getCustomerById(@org.springframework.web.bind.annotation.PathVariable UUID customerId) {
        return customerServiceClient.getCustomerById(new CustomerId(customerId));
    }


    @GetMapping("/flux")
    public Flux<String> getFluxData() {
        return Flux.just("A", "B", "C", "D", "E", "F")
                .log()
                .delayElements(Duration.ofSeconds(1));
    }

//    @Value("${global.version}")
//    private String globalVersion;
//
//    private final ServicePropConfig servicePropConfig;
//
//    @GetMapping("/consul/config")
//    public Mono<Map<String, Object>> getConsulConfig() {
//        return Mono.just(
//                Map.of(
//                        "globalVersion", globalVersion,
//                        "message", servicePropConfig.getMessage(),
//                        "version", servicePropConfig.getVersion(),
//                        "env", servicePropConfig.getEnv(),
//                        "baseUrl", servicePropConfig.getBaseUrl())
//        );
//    }

}
