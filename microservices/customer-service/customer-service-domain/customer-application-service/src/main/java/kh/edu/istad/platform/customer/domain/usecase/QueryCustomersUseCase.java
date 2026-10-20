package kh.edu.istad.platform.customer.domain.usecase;
import reactor.core.publisher.Mono;
import kh.edu.istad.platform.customer.domain.valueobject.CustomerStatus;
import kh.edu.istad.platform.customer.domain.exception.CustomerNotFoundException;
import kh.edu.istad.platform.customer.domain.dto.CustomerStatistics;
import java.util.UUID;

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

    public Mono<Customer> findById(UUID id) {
        return customerRepository.findById(id.toString()).switchIfEmpty(Mono.error(
                new CustomerNotFoundException(id)));
    }

    public Mono<Void> delete(UUID id) {
        return findById(id).flatMap(customer -> customerRepository.deleteById(id.toString()));
    }

    public Flux<Customer> search(String keyword, CustomerStatus status,
                                String segmentId, int page, int size) {
        return customerRepository.search(keyword, status, segmentId, page, size);
    }

    public Mono<CustomerStatistics> statistics() {
        return customerRepository.statistics();
    }

    public Flux<Customer> findAll() {
        return customerRepository.findAll();
    }

}
