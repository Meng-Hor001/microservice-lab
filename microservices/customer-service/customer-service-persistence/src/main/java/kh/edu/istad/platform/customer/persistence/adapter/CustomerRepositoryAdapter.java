package kh.edu.istad.platform.customer.persistence.adapter;

import kh.edu.istad.common.domain.valueobject.CustomerId;
import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.port.out.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class CustomerRepositoryAdapter implements CustomerRepository {
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

    @Override
    public Page<Customer> findAll(Pageable pageable) {
        // Snapshot the Map so the page content and total use the same collection.
        var snapshot = new ArrayList<>(customers.values());
        Comparator<Customer> comparator = (left, right) -> 0;
        for (Sort.Order order : pageable.getSort()) {
            comparator = comparator.thenComparing(comparatorFor(order));
        }
        // A unique tie-breaker keeps page ordering consistent across requests.
        snapshot.sort(comparator.thenComparing(customer -> customer.getId().id()));

        if (pageable.isUnpaged()) {
            return new PageImpl<>(snapshot);
        }

        int start = (int) Math.min(pageable.getOffset(), snapshot.size());
        int end = (int) Math.min((long) start + pageable.getPageSize(), snapshot.size());
        return new PageImpl<>(snapshot.subList(start, end), pageable, snapshot.size());
    }

    private Comparator<Customer> comparatorFor(Sort.Order order) {
        Function<Customer, String> field = switch (order.getProperty()) {
            case "customerId" -> customer -> customer.getId().id().toString();
            case "username" -> Customer::getUsername;
            case "familyName" -> Customer::getFamilyName;
            case "givenName" -> Customer::getGivenName;
            case "email" -> customer -> customer.getEmail() == null ? null : customer.getEmail().value();
            case "phoneNumber" -> customer -> customer.getPhoneNumber() == null ? null : customer.getPhoneNumber().value();
            default -> throw new IllegalArgumentException("Unsupported customer sort field: " + order.getProperty());
        };
        Comparator<String> values = order.isIgnoreCase()
                ? String.CASE_INSENSITIVE_ORDER : Comparator.naturalOrder();
        if (order.isDescending()) {
            values = values.reversed();
        }
        values = order.getNullHandling() == Sort.NullHandling.NULLS_FIRST
                ? Comparator.nullsFirst(values) : Comparator.nullsLast(values);
        return Comparator.comparing(field, values);
    }
}
