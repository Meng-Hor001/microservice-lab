package kh.edu.istad.platform.business.persistence.repository;

import kh.edu.istad.platform.business.persistence.document.OrderApprovalDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface OrderApprovalMongoRepository extends ReactiveMongoRepository<OrderApprovalDocument, String> {
}
