package kh.edu.istad.platform.customer.persistence.repository;

import kh.edu.istad.common.domain.valueobject.CustomerId;
import kh.edu.istad.platform.customer.persistence.Document.CustomerDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface CustomerMongoRepository extends ReactiveMongoRepository<CustomerDocument, String> {
}
