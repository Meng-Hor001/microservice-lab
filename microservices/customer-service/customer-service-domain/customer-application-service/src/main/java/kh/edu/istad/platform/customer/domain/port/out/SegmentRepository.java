package kh.edu.istad.platform.customer.domain.port.out;

import kh.edu.istad.platform.customer.domain.entity.CustomerSegment;
import kh.edu.istad.platform.customer.domain.valueobject.SegmentName;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SegmentRepository {

    Mono<CustomerSegment> save(CustomerSegment segment);

    Mono<CustomerSegment> findById(String id);

    Flux<CustomerSegment> search(
            String keyword,
            SegmentName name,
            int page,
            int size
    );

    Mono<Void> deleteById(String id);
}
