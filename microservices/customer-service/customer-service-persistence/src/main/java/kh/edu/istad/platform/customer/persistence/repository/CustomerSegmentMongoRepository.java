package kh.edu.istad.platform.customer.persistence.repository;

import kh.edu.istad.platform.customer.persistence.Document.CustomerSegmentDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface CustomerSegmentMongoRepository extends ReactiveMongoRepository<CustomerSegmentDocument, String> {
}
