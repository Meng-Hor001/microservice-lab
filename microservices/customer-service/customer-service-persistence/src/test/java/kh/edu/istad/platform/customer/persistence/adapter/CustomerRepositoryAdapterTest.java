package kh.edu.istad.platform.customer.persistence.adapter;

import kh.edu.istad.platform.customer.domain.valueobject.CustomerStatus;
import kh.edu.istad.platform.customer.persistence.Document.CustomerDocument;
import kh.edu.istad.platform.customer.persistence.mapper.CustomerPersistenceMapper;
import kh.edu.istad.platform.customer.persistence.repository.CustomerMongoRepository;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CustomerRepositoryAdapterTest {
    private final ReactiveMongoTemplate template = mock(ReactiveMongoTemplate.class);
    private final CustomerReactiveRepositoryAdapter repository = new CustomerReactiveRepositoryAdapter(
            mock(CustomerMongoRepository.class), mock(CustomerPersistenceMapper.class), template);

    @Test
    void aggregatesStatusSegmentAndGrandTotalsIncludingUnassignedCustomers() {
        when(template.aggregate(any(Aggregation.class), eq(CustomerDocument.class), eq(Document.class)))
                .thenReturn(Flux.just(row("ACTIVE", "gold", 2), row("INACTIVE", "gold", 3L),
                        row("ACTIVE", null, 1)));
        StepVerifier.create(repository.statistics()).assertNext(result -> {
            assertThat(result.totalCustomers()).isEqualTo(6);
            assertThat(result.byStatus()).containsEntry("ACTIVE", 3L).containsEntry("INACTIVE", 3L);
            assertThat(result.bySegment()).containsEntry("gold", 5L).containsEntry("UNASSIGNED", 1L);
            assertThat(result.byStatusAndSegment()).hasSize(3);
        }).verifyComplete();
    }

    @Test
    void emptyCollectionReturnsZeroStatistics() {
        when(template.aggregate(any(Aggregation.class), eq(CustomerDocument.class), eq(Document.class)))
                .thenReturn(Flux.empty());
        StepVerifier.create(repository.statistics()).assertNext(result -> {
            assertThat(result.totalCustomers()).isZero();
            for (CustomerStatus status : CustomerStatus.values()) {
                assertThat(result.byStatus()).containsEntry(status.name(), 0L);
            }
            assertThat(result.bySegment()).isEmpty();
            assertThat(result.byStatusAndSegment()).isEmpty();
        }).verifyComplete();
    }

    private Document row(String status, String segment, Number total) {
        return new Document("_id", new Document("status", status).append("segmentId", segment))
                .append("total", total);
    }
}
