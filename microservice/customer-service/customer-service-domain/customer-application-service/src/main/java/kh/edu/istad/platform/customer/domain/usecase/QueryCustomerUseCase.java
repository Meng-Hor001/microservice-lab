package kh.edu.istad.platform.customer.domain.usecase;

import kh.edu.istad.common.domain.valueobject.CustomerId;
import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.port.out.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class QueryCustomerUseCase {

    private final CustomerRepository customerRepository;

    public Flux<Customer> findAll(){
        return customerRepository.findAll();
    }

    public Mono<Customer> findById(CustomerId id) {
        return customerRepository.findById(id);
    }

    public Mono<Customer> findByUsername(String username) {
        return customerRepository.findByUsername(username);
    }

    public Flux<Customer> query(){
        return customerRepository.query();
    }


}
