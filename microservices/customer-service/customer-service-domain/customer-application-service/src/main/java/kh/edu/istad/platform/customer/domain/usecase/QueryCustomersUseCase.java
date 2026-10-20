package kh.edu.istad.platform.customer.domain.usecase;

import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.port.out.CustomerRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class QueryCustomersUseCase {
    private final CustomerRepository customerRepository;

    public Flux<Customer> findAll() {
        return customerRepository.findAll();
    }

}
