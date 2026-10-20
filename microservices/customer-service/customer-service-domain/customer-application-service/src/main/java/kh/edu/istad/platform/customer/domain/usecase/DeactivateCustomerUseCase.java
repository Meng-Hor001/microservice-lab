package kh.edu.istad.platform.customer.domain.usecase;
import reactor.core.publisher.Mono;
import kh.edu.istad.platform.customer.domain.entity.Customer;

import kh.edu.istad.common.domain.exception.CustomerDomainException;
import kh.edu.istad.common.domain.valueobject.CustomerId;
import kh.edu.istad.platform.customer.domain.event.CustomerDeactivatedEvent;
import kh.edu.istad.platform.customer.domain.exception.CustomerNotFoundException;
import kh.edu.istad.platform.customer.domain.port.out.CustomerRepository;
import kh.edu.istad.platform.customer.domain.service.CustomerDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DeactivateCustomerUseCase {
    private final QueryCustomersUseCase queries;
    private final CustomerRepository repository;

    public Mono<Customer> deactivate(UUID id) {
        return queries.findById(id).flatMap(customer -> {
            customer.deactivateCustomer();
            return repository.save(customer);
        });
    }


}
