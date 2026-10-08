package kh.edu.istad.platform.customer.domain.port.out;

import kh.edu.istad.platform.customer.domain.entity.Customer;

public interface CustomerRepository {
    Customer save(Customer customer);
}
