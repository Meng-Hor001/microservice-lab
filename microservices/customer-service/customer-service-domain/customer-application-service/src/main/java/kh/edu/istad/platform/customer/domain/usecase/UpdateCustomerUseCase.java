package kh.edu.istad.platform.customer.domain.usecase;
import kh.edu.istad.platform.customer.domain.valueobject.Email;
import kh.edu.istad.platform.customer.domain.valueobject.PhoneNumber;
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

    public Mono<Customer> update(UUID id, UpdateCustomerCommand command) {
        return queries.findById(id)
                .flatMap(customer -> {
                    Mono<Void> segmentValidation =
                            command.segmentId() == null
                                    ? Mono.empty()
                                    : segments.findById(command.segmentId()).then();

                    return segmentValidation.then(Mono.defer(() -> {
                        customer.updateCustomer(
                                command.username(),
                                command.familyName(),
                                command.givenName(),
                                new Email(command.email()),
                                new PhoneNumber(command.phoneNumber())
                        );

                        customer.assignSegment(command.segmentId());

                        return repository.save(customer);
                    }));
                });
    }




}
