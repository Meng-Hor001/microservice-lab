package kh.edu.istad.platform.customer.persistence.adapter;

import kh.edu.istad.common.domain.valueobject.CustomerId;
import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.port.out.CustomerRepository;
import kh.edu.istad.platform.customer.persistence.repository.CustomerJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class CustomerRepositoryAdapter implements CustomerRepository {
    private CustomerJpaRepository customerJpaRepository;
    private final Map<CustomerId, Customer> customers = new ConcurrentHashMap<>();
    @Override
    public Customer save(Customer customer) {
        customers.put(customer.getId(), customer);
        return customer;
    }

    @Override
    public Optional<Customer> findById(CustomerId customerId) {
        return Optional.ofNullable(customers.get(customerId));
    }
}
