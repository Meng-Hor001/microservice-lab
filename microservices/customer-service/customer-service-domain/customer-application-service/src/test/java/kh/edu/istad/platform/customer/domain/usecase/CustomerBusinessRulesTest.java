package kh.edu.istad.platform.customer.domain.usecase;

import kh.edu.istad.common.domain.exception.CustomerDomainException;
import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.entity.CustomerSegment;
import kh.edu.istad.platform.customer.domain.exception.CustomerNotFoundException;
import kh.edu.istad.platform.customer.domain.exception.SegmentNotFoundException;
import kh.edu.istad.platform.customer.domain.port.out.CustomerRepository;
import kh.edu.istad.platform.customer.domain.port.out.SegmentRepository;
import kh.edu.istad.platform.customer.domain.valueobject.*;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class CustomerBusinessRulesTest {
    private final CustomerRepository customers = mock(CustomerRepository.class);
    private final SegmentRepository segments = mock(SegmentRepository.class);
    private final SegmentCrudUseCase segmentUseCase = new SegmentCrudUseCase(segments, customers);
    private final QueryCustomersUseCase queries = new QueryCustomersUseCase(customers);

    @Test
    void creationInitializesIdAndStatusAndChecksAssignedSegment() {
        when(segments.findById("gold")).thenReturn(Mono.just(new CustomerSegment("gold", SegmentName.GOLD, null)));
        when(customers.save(any())).thenAnswer(call -> Mono.just(call.getArgument(0, Customer.class)));
        StepVerifier.create(new CreateCustomerUseCase(customers, segmentUseCase).create(customer("gold")))
                .assertNext(saved -> {
                    assertThat(saved.getId()).isNotNull();
                    assertThat(saved.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
                }).verifyComplete();
    }

    @Test
    void unknownSegmentPreventsCustomerCreation() {
        when(segments.findById("missing")).thenReturn(Mono.empty());
        StepVerifier.create(new CreateCustomerUseCase(customers, segmentUseCase).create(customer("missing")))
                .expectError(SegmentNotFoundException.class).verify();
        verify(customers, never()).save(any());
    }

    @Test
    void missingCustomerCannotBeDeleted() {
        var id = UUID.randomUUID();
        when(customers.findById(id.toString())).thenReturn(Mono.empty());
        StepVerifier.create(queries.delete(id)).expectError(CustomerNotFoundException.class).verify();
        verify(customers, never()).deleteById(any());
    }

    @Test
    void segmentInUseCannotBeDeleted() {
        when(segments.findById("gold")).thenReturn(Mono.just(new CustomerSegment("gold", SegmentName.GOLD, null)));
        when(customers.existsBySegmentId("gold")).thenReturn(Mono.just(true));
        StepVerifier.create(segmentUseCase.delete("gold")).expectError(CustomerDomainException.class).verify();
        verify(segments, never()).deleteById(any());
    }

    @Test
    void unusedSegmentCanBeDeleted() {
        when(segments.findById("gold")).thenReturn(Mono.just(new CustomerSegment("gold", SegmentName.GOLD, null)));
        when(customers.existsBySegmentId("gold")).thenReturn(Mono.just(false));
        when(segments.deleteById("gold")).thenReturn(Mono.empty());
        StepVerifier.create(segmentUseCase.delete("gold")).verifyComplete();
        verify(segments).deleteById("gold");
    }

    @Test
    void updateCanRemoveSegmentAndDeactivationRejectsInactiveCustomers() {
        var customer = customer("gold");
        customer.initiateCustomer();
        var id = customer.getId().id();
        when(customers.findById(id.toString())).thenReturn(Mono.just(customer));
        when(customers.save(any())).thenAnswer(call -> Mono.just(call.getArgument(0, Customer.class)));
        StepVerifier.create(new UpdateCustomerUseCase(queries, customers, segmentUseCase)
                .update(id, "New", "Name", null)).assertNext(saved -> {
                    assertThat(saved.getFamilyName()).isEqualTo("New");
                    assertThat(saved.getGivenName()).isEqualTo("Name");
                    assertThat(saved.getSegmentId()).isNull();
                }).verifyComplete();
        var deactivation = new DeactivateCustomerUseCase(queries, customers);
        StepVerifier.create(deactivation.deactivate(id))
                .assertNext(saved -> assertThat(saved.getStatus()).isEqualTo(CustomerStatus.INACTIVE)).verifyComplete();
        StepVerifier.create(deactivation.deactivate(id)).expectError(CustomerDomainException.class).verify();
    }

    private Customer customer(String segment) {
        return Customer.builder().username("alice").familyName("Smith").givenName("Alice")
                .email(new Email("alice@example.com")).phoneNumber(new PhoneNumber("012345678"))
                .segmentId(segment).build();
    }
}
