package kh.edu.istad.platform.order.persistence.adapter;

import kh.edu.istad.platform.order.domain.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import kh.edu.istad.platform.order.application.port.out.OrderRepository;
import kh.edu.istad.platform.order.domain.entity.Order;
import kh.edu.istad.platform.order.persistence.mapper.OrderPersistenceMapper;
import kh.edu.istad.platform.order.persistence.repository.OrderJpaRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Repository
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepository {

    private final OrderJpaRepository jpaRepository;
    private final OrderPersistenceMapper orderPersistenceMapper;

    @Override
    public Page<Order> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(orderPersistenceMapper::toDomain);
    }

    @Override
    @Transactional
    public Order save(Order order) {
        var entity = orderPersistenceMapper.toEntity(order);
        jpaRepository.saveAndFlush(entity);
        return order;
    }

    @Override
    @Transactional
    public void applyApproval(UUID orderId, UUID approvalId, UUID businessId, String status, List<String> failureMessages) {
        var entity = jpaRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new DomainException("Order not found"));

        if (!entity.getBusinessId().equals(businessId)) {
            throw new DomainException("Order belongs to another business");
        }

        if (!"APPROVED".equals(status) && !"REJECTED".equals(status)) {
            throw new DomainException("Invalid approval status");
        }

        if ("APPROVED".equals(status) && !failureMessages.isEmpty()) {
            throw new DomainException("Approved decision cannot contain failures");
        }

        if ("REJECTED".equals(status) && failureMessages.isEmpty()) {
            throw new DomainException("Rejected decision requires a failure reason");
        }
        if (entity.getBusinessApprovalId() != null) {
            boolean sameOutcome = "APPROVED".equals(status)
                    ? "APPROVED".equals(entity.getStatus())
                    : ("CANCELLING".equals(entity.getStatus())
                    || "CANCELLED".equals(entity.getStatus()));

            if (approvalId.equals(entity.getBusinessApprovalId())
                    && sameOutcome
                    && entity.getFailureMessages().equals(failureMessages)) {
                return;
            }

            throw new DomainException("A different decision was already applied");
        }
        var order = orderPersistenceMapper.toDomain(entity);

        // These domain methods require the actual saved order to be PAID.
        var updated = "APPROVED".equals(status)
                ? order.approve()
                : order.initCancel(failureMessages);

        entity.setStatus(updated.status().name());
        entity.setFailureMessages(
                new ArrayList<>(updated.failureMessages())
        );
        entity.setBusinessApprovalId(approvalId);

        jpaRepository.saveAndFlush(entity);
    }
}
