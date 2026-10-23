package kh.edu.istad.platform.customer.persistence.adapter;

import kh.edu.istad.common.domain.valueobject.CustomerId;
import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.valueobject.CustomerStatus;
import kh.edu.istad.platform.customer.persistence.entity.CustomerEntity;
import kh.edu.istad.platform.customer.persistence.mapper.CustomerPersistenceMapper;
import kh.edu.istad.platform.customer.persistence.repository.CustomerJpaRepository;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CustomerRepositoryAdapterTest {

    private final CustomerJpaRepository jpaRepository = mock(CustomerJpaRepository.class);
    private final CustomerPersistenceMapper mapper = Mappers.getMapper(CustomerPersistenceMapper.class);
    private final CustomerRepositoryAdapter repository = new CustomerRepositoryAdapter(jpaRepository, mapper);

    @Test
    void savesMappedCustomerAndReturnsPersistedValues() {
        var entity = customerEntity();
        var customer = mapper.toDomain(entity);
        when(jpaRepository.save(any(CustomerEntity.class))).thenAnswer(invocation -> {
            CustomerEntity saved = invocation.getArgument(0);
            assertThat(saved).usingRecursiveComparison().isEqualTo(entity);
            saved.setUsername("Persisted name");
            return saved;
        });

        var saved = repository.save(customer);

        assertThat(saved.getId()).isEqualTo(customer.getId());
        assertThat(saved.getUsername()).isEqualTo("Persisted name");
        assertThat(saved.getEmail()).isEqualTo(customer.getEmail());
        assertThat(saved.getPhoneNumber()).isEqualTo(customer.getPhoneNumber());
        assertThat(saved.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
    }

    @Test
    void findsCustomerByUuidAndMapsValueObjects() {
        var entity = customerEntity();
        when(jpaRepository.findById(entity.getCustomerId())).thenReturn(Optional.of(entity));

        var customer = repository.findById(new CustomerId(entity.getCustomerId())).orElseThrow();

        assertThat(customer.getId().id()).isEqualTo(entity.getCustomerId());
        assertThat(customer.getEmail().value()).isEqualTo(entity.getEmail());
        assertThat(customer.getPhoneNumber().value()).isEqualTo(entity.getPhoneNumber());
        assertThat(customer.getFamilyName()).isEqualTo(entity.getFamilyName());
        assertThat(customer.getGivenName()).isEqualTo(entity.getGivenName());
    }

    @Test
    void missingCustomerReturnsEmptyOptional() {
        var id = UUID.randomUUID();
        when(jpaRepository.findById(id)).thenReturn(Optional.empty());

        assertThat(repository.findById(new CustomerId(id))).isEmpty();
    }

    @Test
    void preservesJpaPageMetadataAndSortWhenMappingCustomers() {
        var pageable = PageRequest.of(1, 2, Sort.by("username"));
        var entity = customerEntity();
        when(jpaRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(entity), pageable, 3));

        var page = repository.findAll(pageable);

        assertThat(page.getContent()).extracting(Customer::getUsername).containsExactly("Alice");
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getPageable()).isEqualTo(pageable);
    }

    private CustomerEntity customerEntity() {
        var entity = new CustomerEntity();
        entity.setCustomerId(UUID.randomUUID());
        entity.setUsername("Alice");
        entity.setFamilyName("Smith");
        entity.setGivenName("Alice");
        entity.setEmail("alice@example.com");
        entity.setPhoneNumber("012345678");
        entity.setStatus(CustomerStatus.ACTIVE);
        return entity;
    }
}
