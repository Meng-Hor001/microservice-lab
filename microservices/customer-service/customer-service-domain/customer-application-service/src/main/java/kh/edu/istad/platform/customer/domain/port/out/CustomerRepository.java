package kh.edu.istad.platform.customer.domain.port.out;

import kh.edu.istad.platform.customer.domain.entity.Customer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface CustomerRepository {

    Mono<Customer> save(Customer customer);

    Flux<Customer> findAll();
}
