package kh.edu.istad.platform.business.persistence.adapter;

import java.util.UUID;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import kh.edu.istad.platform.business.application.port.out.BusinessRepository;
import kh.edu.istad.platform.business.domain.entity.*;
import kh.edu.istad.platform.business.domain.valueobject.*;
import kh.edu.istad.platform.business.persistence.document.BusinessDocument;
import kh.edu.istad.platform.business.persistence.document.BusinessDocument.ProductDocument;
import kh.edu.istad.platform.business.persistence.repository.BusinessMongoRepository;

@Repository
public class BusinessRepositoryAdapter implements BusinessRepository {
    private final BusinessMongoRepository repository;
    public BusinessRepositoryAdapter(BusinessMongoRepository repository) { this.repository = repository; }
    public Mono<Business> save(Business business) {
        var document = new BusinessDocument(business.id().value().toString(), business.name().legalName(),
                business.name().displayName(), business.contact().email(), business.contact().phoneNumber(),
                business.active(), business.products().stream().map(p -> new ProductDocument(
                    p.productId().value().toString(), p.name(), p.price().amount(), p.quantity(), p.available())).toList());
        return repository.save(document).map(d -> new Business(new BusinessId(UUID.fromString(d.id())),
                new BusinessName(d.legalName(), d.displayName()), new BusinessContact(d.email(), d.phoneNumber()),
                d.active(), d.products().stream().map(p -> new Product(new ProductId(UUID.fromString(p.id())),
                    p.name(), new Money(p.price()), p.quantity(), p.available())).toList()));
    }
}
