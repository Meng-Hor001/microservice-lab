package kh.edu.istad.platform.business.application.usecase;

import kh.edu.istad.platform.business.application.port.out.BusinessRepository;
import kh.edu.istad.platform.business.application.port.out.OrderApprovalPublisher;
import kh.edu.istad.platform.business.application.port.out.OrderApprovalRepository;
import kh.edu.istad.platform.business.domain.entity.OrderApproval;
import kh.edu.istad.platform.business.domain.entity.OrderDetail;
import kh.edu.istad.platform.business.domain.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ValidateOrderUseCase {
    private final BusinessRepository businessRepository;
    private final OrderApprovalRepository approvalRepository;
    private final OrderApprovalPublisher approvalPublisher;

    public Mono<OrderApproval> execute(
            UUID businessId,
            OrderDetail orderDetail) {

        return approvalRepository
                .findByOrderId(orderDetail.orderId().value())
                .switchIfEmpty(Mono.defer(() ->
                        businessRepository.findById(businessId)
                                .switchIfEmpty(Mono.error(
                                        new DomainException("Business not found")
                                ))
                                .map(business -> business.approve(orderDetail))
                                .flatMap(approvalRepository::insert)
                ))
                .flatMap(approval -> {
                    if (!approval.businessId().value().equals(businessId)) {
                        return Mono.error(new DomainException(
                                "Order decision belongs to another business"
                        ));
                    }

                    return approvalPublisher.publish(approval)
                            .thenReturn(approval);
                });
    }

}
