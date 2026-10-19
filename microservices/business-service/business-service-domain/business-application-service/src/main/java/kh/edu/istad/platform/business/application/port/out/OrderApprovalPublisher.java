package kh.edu.istad.platform.business.application.port.out;

import kh.edu.istad.platform.business.domain.entity.OrderApproval;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface OrderApprovalPublisher {
    Mono<Void> publish(OrderApproval orderApproval);
}
