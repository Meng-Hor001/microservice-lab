package kh.edu.istad.platform.business.application.port.out;

import kh.edu.istad.platform.business.domain.entity.OrderApproval;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OrderApprovalRepository {
    Mono<OrderApproval> findByOrderId(UUID orderId);

    Mono<OrderApproval> insert(OrderApproval approval);
}
