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

    public Mono<Customer> create(Customer customer) {
        customer.initiateCustomer();
        return customerRepository.save(customer);
    }
}
