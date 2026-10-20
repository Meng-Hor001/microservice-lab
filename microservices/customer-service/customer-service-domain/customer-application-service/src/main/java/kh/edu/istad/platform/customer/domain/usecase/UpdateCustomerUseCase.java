package kh.edu.istad.platform.customer.domain.usecase;
import reactor.core.publisher.Mono;
import java.util.UUID;

import kh.edu.istad.common.domain.exception.CustomerDomainException;
import kh.edu.istad.common.domain.valueobject.CustomerId;
import kh.edu.istad.platform.customer.domain.dto.UpdateCustomerCommand;
import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.event.CustomerUpdatedEvent;
import kh.edu.istad.platform.customer.domain.exception.CustomerNotFoundException;
import kh.edu.istad.platform.customer.domain.port.out.CustomerRepository;
import kh.edu.istad.platform.customer.domain.service.CustomerDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateCustomerUseCase {
    private final QueryCustomersUseCase queries;
    private final CustomerRepository repository;
    private final SegmentCrudUseCase segments;

    public Mono<Customer> update(UUID id, String familyName, String givenName, String segmentId) {
        return queries.findById(id).flatMap(customer -> {
            customer.updateCustomer(familyName, givenName);
            customer.assignSegment(segmentId);
            return (segmentId == null ? Mono.empty() : segments.findById(segmentId))
                    .then(Mono.defer(() -> repository.save(customer)));
        });
    }



}
