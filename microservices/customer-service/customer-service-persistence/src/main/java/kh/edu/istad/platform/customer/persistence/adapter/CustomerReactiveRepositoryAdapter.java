package kh.edu.istad.platform.customer.persistence.adapter;

import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.port.out.CustomerRepository;
import kh.edu.istad.platform.customer.persistence.Document.CustomerDocument;
import kh.edu.istad.platform.customer.persistence.mapper.CustomerPersistenceMapper;
import kh.edu.istad.platform.customer.persistence.repository.CustomerMongoRepository;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class CustomerReactiveRepositoryAdapter implements CustomerRepository {

    private final CustomerMongoRepository customerMongoRepository;
    private final CustomerPersistenceMapper customerPersistenceMapper;

    @Override
    public Mono<Customer> save(Customer customer) {
        CustomerDocument customerDocument = customerPersistenceMapper.toDocument(customer);
        Mono<CustomerDocument> result = customerMongoRepository.save(customerDocument);
        return result.map(customerPersistenceMapper::toDomain);
    }

    @Override
    public Flux<Customer> findAll() {
        return customerMongoRepository.findAll().map(customerPersistenceMapper::toDomain);
    }
}
