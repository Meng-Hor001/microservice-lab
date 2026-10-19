package kh.edu.istad.platform.business.persistence.adapter;

import com.mongodb.DuplicateKeyException;
import kh.edu.istad.platform.business.application.port.out.OrderApprovalRepository;
import kh.edu.istad.platform.business.domain.entity.OrderApproval;
import kh.edu.istad.platform.business.domain.valueobject.BusinessId;
import kh.edu.istad.platform.business.domain.valueobject.OrderApprovalId;
import kh.edu.istad.platform.business.domain.valueobject.OrderApprovalStatus;
import kh.edu.istad.platform.business.domain.valueobject.OrderId;
import kh.edu.istad.platform.business.persistence.document.OrderApprovalDocument;
import kh.edu.istad.platform.business.persistence.repository.OrderApprovalMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;


import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OrderApprovalRepositoryAdapter implements OrderApprovalRepository {
    private final OrderApprovalMongoRepository orderApprovalMongoRepository;

    @Override
    public Mono<OrderApproval> findByOrderId(UUID orderId) {
        return orderApprovalMongoRepository.findById(orderId.toString())
                .map(this::toDomain);
    }

    @Override
    public Mono<OrderApproval> insert(OrderApproval approval) {
        var document = new OrderApprovalDocument(
                approval.orderId().value().toString(),
                approval.id().value(),
                approval.businessId().value(),
                approval.status().name(),
                approval.failureMessages()
        );
        return orderApprovalMongoRepository.insert(document)
                .map(this::toDomain)
                // Another request may have saved this order's decision first.
                .onErrorResume(DuplicateKeyException.class,
                        error -> findByOrderId(approval.orderId().value()));
    }

    private OrderApproval toDomain(OrderApprovalDocument document) {
        return new OrderApproval(
                new OrderApprovalId(UUID.fromString(String.valueOf(document.approvalId()))),
                new BusinessId(UUID.fromString(String.valueOf(document.businessId()))),
                new OrderId(UUID.fromString(document.orderId())),
                OrderApprovalStatus.valueOf(document.status()),
                document.failureMessages()
        );
    }

}
