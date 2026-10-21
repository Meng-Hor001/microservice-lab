package kh.edu.istad.platform.customer.persistence.adapter;

import kh.edu.istad.platform.customer.domain.entity.CustomerSegment;
import kh.edu.istad.platform.customer.domain.exception.DuplicateResourceException;
import kh.edu.istad.platform.customer.domain.port.out.SegmentRepository;
import kh.edu.istad.platform.customer.domain.valueobject.SegmentName;
import kh.edu.istad.platform.customer.persistence.Document.CustomerSegmentDocument;
import kh.edu.istad.platform.customer.persistence.mapper.SegmentPersistenceMapper;
import kh.edu.istad.platform.customer.persistence.query.DynamicMongoQuery;
import kh.edu.istad.platform.customer.persistence.repository.CustomerSegmentMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class SegmentRepositoryAdapter implements SegmentRepository {

    private final CustomerSegmentMongoRepository mongoRepository;
    private final ReactiveMongoTemplate reactiveMongoTemplate;
    private final SegmentPersistenceMapper mapper;

    @Override
    public Mono<CustomerSegment> save(CustomerSegment segment) {
        return mongoRepository.save(mapper.toDocument(segment))
                .map(mapper::toDomain)
                .onErrorMap(
                        DuplicateKeyException.class,
                        error -> new DuplicateResourceException(
                                "Segment name already exists"
                        )
                );
    }

    @Override
    public Mono<CustomerSegment> findById(String id) {
        return mongoRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Flux<CustomerSegment> search(String keyword, SegmentName name, int page, int size) {
        var query = new DynamicMongoQuery()
                .equal("name", name)
                .keyword(keyword, "name", "description")
                .page(
                        page,
                        size,
                        Sort.by(
                                Sort.Order.asc("name"),
                                Sort.Order.desc("id")
                        )
                );

        return reactiveMongoTemplate
                .find(query, CustomerSegmentDocument.class)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return mongoRepository.deleteById(id);
    }

}
