package kh.edu.istad.platform.customer.domain.usecase;

import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.port.out.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class CreateCustomerUseCase {

    private final CustomerRepository customerRepository;

    public Mono<Customer> create(Customer customer){
        customer.initiateCustomer();
        return customerRepository.save(customer);
    }

}
