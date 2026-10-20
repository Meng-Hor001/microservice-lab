package kh.edu.istad.platform.customer.domain.port.out;
import kh.edu.istad.platform.customer.domain.valueobject.CustomerStatus;
import kh.edu.istad.platform.customer.domain.dto.CustomerStatistics;

import kh.edu.istad.platform.customer.domain.entity.Customer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface CustomerRepository {

    Mono<Customer> save(Customer customer);

    Flux<Customer> findAll();
    Mono<Customer> findById(String id);
    Mono<Void> deleteById(String id);
    Mono<Boolean> existsBySegmentId(String segmentId);
    Flux<Customer> search(String keyword, CustomerStatus status,
                          String segmentId, int page, int size);
    Mono<CustomerStatistics> statistics();
}
