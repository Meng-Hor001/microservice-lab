package kh.edu.istad.platform.order.domain.port.out.client;

import kh.edu.istad.platform.order.domain.dto.QueryCustomerResult;
import kh.edu.istad.common.domain.valueobject.CustomerId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerServiceClient {

    Flux<QueryCustomerResult> getCustomerNames();

    Mono<QueryCustomerResult> getCustomerById(CustomerId customerId);

}
