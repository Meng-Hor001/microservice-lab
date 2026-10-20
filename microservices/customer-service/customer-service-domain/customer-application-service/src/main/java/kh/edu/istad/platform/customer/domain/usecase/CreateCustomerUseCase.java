package kh.edu.istad.platform.customer.domain.usecase;

import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.port.out.CustomerRepository;
import kh.edu.istad.platform.customer.domain.service.CustomerDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CreateCustomerUseCase {

    private final CustomerRepository customerRepository;

    private final SegmentCrudUseCase segments;

    public Mono<Customer> create(Customer customer) {
        return Mono.defer(() -> {
            customer.initiateCustomer();
            return (customer.getSegmentId() == null ? Mono.empty() : segments.findById(customer.getSegmentId()))
                    .then(Mono.defer(() -> customerRepository.save(customer)));
        });
    }
}
