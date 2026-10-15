package kh.edu.istad.platform.business.domain.entity;

import java.util.List;
import java.util.Objects;

import kh.edu.istad.platform.business.domain.exception.DomainException;
import kh.edu.istad.platform.business.domain.valueobject.*;
public record OrderApproval(
        OrderApprovalId id,
        BusinessId businessId,
        OrderId orderId,
        OrderApprovalStatus status,
        List<String> failureMessages) {

    public OrderApproval {
        if (id == null || businessId == null || orderId == null || status == null){
            throw new DomainException("Approval ID, business ID, order ID, and status are required");
        }

        if (failureMessages == null || failureMessages.stream().anyMatch(Objects::isNull)) {
            throw new DomainException("Failure messages must be a list without null entries");
        }

        if (status == OrderApprovalStatus.APPROVED && !failureMessages.isEmpty()){
            throw new DomainException("An approved order cannot have failure messages");
        }

        if (status == OrderApprovalStatus.REJECTED
                && failureMessages.isEmpty()) {
            throw new DomainException(
                    "A rejected order must have a failure reason"
            );
        }

        failureMessages = List.copyOf(failureMessages);
    }

}
