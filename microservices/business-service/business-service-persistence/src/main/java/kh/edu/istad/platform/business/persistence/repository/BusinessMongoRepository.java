package kh.edu.istad.platform.business.persistence.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import kh.edu.istad.platform.business.persistence.document.BusinessDocument;
public interface BusinessMongoRepository extends ReactiveMongoRepository<BusinessDocument, String> {}
