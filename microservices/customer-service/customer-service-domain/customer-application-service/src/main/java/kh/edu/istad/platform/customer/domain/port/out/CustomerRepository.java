package kh.edu.istad.platform.customer.domain.port.out;

import kh.edu.istad.common.domain.valueobject.CustomerId;
import kh.edu.istad.platform.customer.domain.entity.Customer;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerRepository {

    Customer save(Customer customer);

    Optional<Customer> findById(CustomerId customerId);

    Page<Customer> findAll(Pageable pageable);
}
