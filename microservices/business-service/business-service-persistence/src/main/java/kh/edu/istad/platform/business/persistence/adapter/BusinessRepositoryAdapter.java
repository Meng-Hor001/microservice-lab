package kh.edu.istad.platform.business.persistence.adapter;

import java.util.UUID;

import kh.edu.istad.platform.business.domain.entity.Business;
import kh.edu.istad.platform.business.domain.entity.Product;
import kh.edu.istad.platform.business.domain.valueobject.*;
import kh.edu.istad.platform.business.persistence.document.BusinessDocument;
import kh.edu.istad.platform.business.persistence.document.ProductDocument;
import kh.edu.istad.platform.business.persistence.repository.BusinessMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import kh.edu.istad.platform.business.application.port.out.BusinessRepository;


@Repository
@RequiredArgsConstructor
public class BusinessRepositoryAdapter implements BusinessRepository {

    private final BusinessMongoRepository businessMongoRepository;
    @Override
    public Mono<Business> save(Business business) {
        return Mono.defer(() -> {
            var products = business.products().stream()
                    .map(product -> new ProductDocument(
                            product.productId().value().toString(),
                            product.name(),
                            product.price().amount(),
                            product.quantity(),
                            product.available()
                    ))
                    .toList();

            var document = new BusinessDocument(
                    business.id().value().toString(),
                    business.name().legalName(),
                    business.name().displayName(),
                    business.contact().email(),
                    business.contact().phoneNumber(),
                    business.active(),
                    products
            );

            return businessMongoRepository.save(document)
                    .map(this::toDomain);
        });
    }

    private Business toDomain(BusinessDocument document) {
        var products = document.products().stream()
                .map(product -> new Product(
                        new ProductId(UUID.fromString(product.id())),
                        product.name(),
                        new Money(product.price()),
                        product.quantity(),
                        product.available()
                ))
                .toList();

        return new Business(
                new BusinessId(UUID.fromString(document.id())),
                new BusinessName(
                        document.legalName(),
                        document.displayName()
                ),
                new BusinessContact(
                        document.email(),
                        document.phoneNumber()
                ),
                document.active(),
                products
        );
    }
}
