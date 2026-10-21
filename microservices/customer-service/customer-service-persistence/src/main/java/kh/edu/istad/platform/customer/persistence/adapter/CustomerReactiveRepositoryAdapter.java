package kh.edu.istad.platform.customer.persistence.adapter;
import kh.edu.istad.platform.customer.domain.exception.DuplicateResourceException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.domain.Sort;
import kh.edu.istad.platform.customer.persistence.query.DynamicMongoQuery;
import kh.edu.istad.platform.customer.domain.valueobject.CustomerStatus;
import kh.edu.istad.platform.customer.domain.dto.CustomerStatistics;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;

import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.port.out.CustomerRepository;
import kh.edu.istad.platform.customer.persistence.Document.CustomerDocument;
import kh.edu.istad.platform.customer.persistence.mapper.CustomerPersistenceMapper;
import kh.edu.istad.platform.customer.persistence.repository.CustomerMongoRepository;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class CustomerReactiveRepositoryAdapter implements CustomerRepository {

    private final CustomerMongoRepository customerMongoRepository;
    private final CustomerPersistenceMapper customerPersistenceMapper;

    private final ReactiveMongoTemplate template;

    @Override
    public Mono<Customer> findById(String id) {
        return customerMongoRepository.findById(id).map(customerPersistenceMapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(String id) { return customerMongoRepository.deleteById(id); }

    @Override
    public Mono<Boolean> existsBySegmentId(String segmentId) {
        return template.exists(Query.query(
                Criteria.where("segmentId").is(segmentId)), CustomerDocument.class);
    }

    @Override
    public Flux<Customer> search(String keyword, CustomerStatus status,
                                 String segmentId, int page, int size) {
        return Flux.defer(() -> template.find(new DynamicMongoQuery()
                .equal("status", status).equal("segmentId", segmentId)
                .keyword(keyword, "username", "familyName", "givenName", "email")
                .page(page, size, Sort.by("username", "id")), CustomerDocument.class)
                .map(customerPersistenceMapper::toDomain));
    }

    @Override
    public Mono<CustomerStatistics> statistics() {
        var aggregation = Aggregation.newAggregation(
                Aggregation.group("status", "segmentId").count().as("total"),
                Aggregation.sort(Sort.Direction.ASC, "_id.status", "_id.segmentId"));
        return template.aggregate(aggregation, CustomerDocument.class, Document.class).collectList().map(rows -> {
            Map<String, Long> statuses = new LinkedHashMap<>();
            for (var status : CustomerStatus.values()) statuses.put(status.name(), 0L);
            Map<String, Long> segments = new LinkedHashMap<>();
            var groups = new ArrayList<CustomerStatistics.Group>();
            long total = 0;
            for (var row : rows) {
                var key = row.get("_id", Document.class);
                String status = key.getString("status");
                String segment = key.getString("segmentId");
                long count = ((Number) row.get("total")).longValue();
                statuses.merge(status == null ? "UNKNOWN" : status, count, Long::sum);
                segments.merge(segment == null ? "UNASSIGNED" : segment, count, Long::sum);
                groups.add(new CustomerStatistics.Group(status, segment, count));
                total += count;
            }
            return new CustomerStatistics(total, statuses, segments, groups);
        });
    }

    @Override
    public Mono<Customer> save(Customer customer) {
        return customerMongoRepository
                .save(customerPersistenceMapper.toDocument(customer))
                .map(customerPersistenceMapper::toDomain)
                .onErrorMap(
                        DuplicateKeyException.class,
                        error -> new DuplicateResourceException(
                                "Customer username or email already exists"
                        )
                );
    }

    @Override
    public Flux<Customer> findAll() {
        return customerMongoRepository.findAll().map(customerPersistenceMapper::toDomain);
    }
}
