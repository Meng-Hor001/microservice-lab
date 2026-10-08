package kh.edu.istad.platform.customer.persistence.adapter;

import kh.edu.istad.common.domain.valueobject.CustomerId;
import kh.edu.istad.platform.customer.domain.entity.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomerRepositoryAdapterTest {

    private final CustomerRepositoryAdapter repository = new CustomerRepositoryAdapter();

    @Test
    void emptyRepositoryReturnsEmptyPage() {
        var page = repository.findAll(PageRequest.of(0, 10));

        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isZero();
    }

    @Test
    void sortsBeforePagingAndPreservesTotals() {
        addCustomer(3, "Charlie");
        addCustomer(1, "Alice");
        addCustomer(2, "Bob");

        var page = repository.findAll(PageRequest.of(1, 2, Sort.by("username")));

        assertThat(page.getContent()).extracting(Customer::getUsername).containsExactly("Charlie");
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getNumber()).isEqualTo(1);

        var descending = repository.findAll(PageRequest.of(0, 2, Sort.by("username").descending()));
        assertThat(descending.getContent()).extracting(Customer::getUsername).containsExactly("Charlie", "Bob");
    }

    @Test
    void usesCustomerIdToBreakSortTies() {
        var second = addCustomer(2, "Same");
        var first = addCustomer(1, "Same");

        assertThat(repository.findAll(PageRequest.of(0, 1, Sort.by("username"))).getContent())
                .containsExactly(first);
        assertThat(repository.findAll(PageRequest.of(1, 1, Sort.by("username"))).getContent())
                .containsExactly(second);
    }

    @Test
    void pageBeyondEndIncludingLargeOffsetIsEmpty() {
        addCustomer(1, "Alice");

        var page = repository.findAll(PageRequest.of(Integer.MAX_VALUE, 10));

        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isEqualTo(1);
    }

    @Test
    void rejectsUnsupportedSortEvenWhenRepositoryIsEmpty() {
        assertThatThrownBy(() -> repository.findAll(PageRequest.of(0, 10, Sort.by("unknown"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported customer sort field");
    }

    private Customer addCustomer(long id, String username) {
        return repository.save(Customer.Builder.builder()
                .id(new CustomerId(new UUID(0, id)))
                .username(username)
                .build());
    }
}
