package kh.edu.istad.platform.order.client.adapter;

import kh.edu.istad.platform.order.client.httpclient.CustomerHttpClient;
import kh.edu.istad.common.domain.valueobject.CustomerId;
import build.buf.gen.customer.v1.CustomerServiceGrpc;
import build.buf.gen.customer.v1.GetCustomerRequest;
import kh.edu.istad.platform.order.domain.dto.QueryCustomerResult;
import kh.edu.istad.platform.order.domain.port.out.client.CustomerServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@RequiredArgsConstructor
public class CustomerServiceClientAdapter implements CustomerServiceClient {

    private final CustomerHttpClient customerHttpClient;
    private final CustomerServiceGrpc.CustomerServiceBlockingStub customerServiceBlockingStub;

    @Override
    public Flux<QueryCustomerResult> getCustomerNames() {











        return customerHttpClient.queryCustomer();
    }

    @Override
    public Mono<QueryCustomerResult> getCustomerById(CustomerId customerId) {
        return Mono.fromCallable(() -> customerServiceBlockingStub.getCustomer(
                        GetCustomerRequest.newBuilder()
                                .setUsername(customerId.value().toString())
                                .build()))
                .map(response -> new QueryCustomerResult(
                        response.getFamilyName(),
                        response.getGivenName()))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
