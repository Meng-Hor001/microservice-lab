package kh.edu.istad.platform.customer.persistence.adapter;

import kh.edu.istad.common.domain.valueobject.CustomerId;
import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.port.out.CustomerRepository;
import kh.edu.istad.platform.customer.domain.valueobject.Email;
import kh.edu.istad.platform.customer.domain.valueobject.PhoneNumber;
import kh.edu.istad.platform.customer.persistence.entity.CustomerEntity;
import kh.edu.istad.platform.customer.persistence.repository.CustomerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@RequiredArgsConstructor
public class CustomerRepositoryAdapter implements CustomerRepository {
    private final CustomerJpaRepository customerJpaRepository;
    @Override
    public Customer save(Customer customer) {
        CustomerEntity entity = toEntity(customer);
        CustomerEntity saveEntity = customerJpaRepository.save(entity);
        return toDomain(saveEntity);
    }

    @Override
    public Optional<Customer> findById(CustomerId customerId) {
        return customerJpaRepository.findById(customerId.id()).map(this::toDomain);
    }

    @Override
    public Page<Customer> findAll(Pageable pageable) {
        return customerJpaRepository.findAll(pageable)
                .map(this::toDomain);
    }

    private CustomerEntity toEntity(Customer customer) {
            CustomerEntity entity = new CustomerEntity();

            entity.setCustomerId(customer.getId().id());
            entity.setUsername(customer.getUsername());
            entity.setFamilyName(customer.getFamilyName());
            entity.setGivenName(customer.getGivenName());
            entity.setEmail(customer.getEmail().value());
            entity.setPhoneNumber(customer.getPhoneNumber().value());
            entity.setStatus(customer.getStatus());

            return entity;
        }

        private Customer toDomain(CustomerEntity entity) {
            return Customer.Builder.builder()
                    .id(new CustomerId(entity.getCustomerId()))
                    .username(entity.getUsername())
                    .familyName(entity.getFamilyName())
                    .givenName(entity.getGivenName())
                    .email(new Email(entity.getEmail()))
                    .phoneNumber(new PhoneNumber(entity.getPhoneNumber()))
                    .status(entity.getStatus())
                    .build();
        }
    }
