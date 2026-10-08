package kh.edu.istad.platform.customer.domain.usecase;

import kh.edu.istad.common.domain.exception.CustomerDomainException;
import kh.edu.istad.common.domain.valueobject.CustomerId;
import kh.edu.istad.platform.customer.domain.event.CustomerDeactivatedEvent;
import kh.edu.istad.platform.customer.domain.port.out.CustomerRepository;
import kh.edu.istad.platform.customer.domain.service.CustomerDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DeactivateCustomerUseCase {
    private final CustomerRepository customerRepository;
    private final CustomerDomainService customerDomainService;

    public CustomerDeactivatedEvent execute(UUID customerId){
        var customer = customerRepository.findById(
                new CustomerId(customerId)
        ).orElseThrow(
                ()-> new CustomerDomainException("Customer not found")
        );

        var event = customerDomainService.deactivateCustomer(customer);

        customerRepository.save(customer);

        return event;
    }
}
