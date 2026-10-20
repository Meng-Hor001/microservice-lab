package kh.edu.istad.platform.customer.domain.usecase;
import kh.edu.istad.platform.customer.domain.port.out.CustomerRepository;
import kh.edu.istad.common.domain.exception.CustomerDomainException;

import kh.edu.istad.platform.customer.domain.entity.CustomerSegment;
import kh.edu.istad.platform.customer.domain.exception.SegmentNotFoundException;
import kh.edu.istad.platform.customer.domain.port.out.SegmentRepository;
import kh.edu.istad.platform.customer.domain.valueobject.SegmentName;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SegmentCrudUseCase
{
    private final SegmentRepository segmentRepository;
    private final CustomerRepository customers;

    public Mono<CustomerSegment> create(
            SegmentName name,
            String description
    ){
        return Mono.defer(() -> {
            CustomerSegment segment = new CustomerSegment(
                    UUID.randomUUID().toString(),
                    name,
                    description
            );
            return segmentRepository.save(segment);
        });
    }

    public Mono<CustomerSegment> findById(String id){
        return segmentRepository.findById(id)
                .switchIfEmpty(
                        Mono.error(new SegmentNotFoundException(id))
                );
    }

    public Flux<CustomerSegment> search(
            String keyword,
            SegmentName name,
            int page,
            int size
    ) {
        return segmentRepository.search(
                keyword,
                name,
                page,
                size
        );
    }

    public Mono<CustomerSegment> update(
            String id,
            SegmentName name,
            String description
    ) {
        return findById(id)
                .flatMap(existing -> {
                    CustomerSegment updated = new CustomerSegment(
                            existing.id(),
                            name,
                            description
                    );
                    return segmentRepository.save(updated);
                });
    }

    public Mono<Void> delete(String id) {
        return findById(id)
                .flatMap(existing ->
                        customers.existsBySegmentId(existing.id()).flatMap(inUse -> inUse
                                ? Mono.error(new CustomerDomainException("Cannot delete a segment assigned to customers"))
                                : segmentRepository.deleteById(existing.id()))
                );
    }
}
