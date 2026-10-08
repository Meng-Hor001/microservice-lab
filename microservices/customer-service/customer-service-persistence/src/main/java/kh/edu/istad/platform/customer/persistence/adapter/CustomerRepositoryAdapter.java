package kh.edu.istad.platform.customer.persistence.adapter;

import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.port.out.CustomerRepository;
import kh.edu.istad.platform.customer.persistence.entity.CustomerEntity;
import kh.edu.istad.platform.customer.persistence.repository.CustomerJpaRepository;

public class CustomerRepositoryAdapter implements CustomerRepository {
    private CustomerJpaRepository customerJpaRepository;
    @Override
    public Customer save(Customer customer) {
        return null;
    }
}
