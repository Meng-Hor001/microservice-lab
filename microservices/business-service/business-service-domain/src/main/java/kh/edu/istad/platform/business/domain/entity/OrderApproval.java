package kh.edu.istad.platform.business.domain.entity;

import java.util.List;
import kh.edu.istad.platform.business.domain.valueobject.*;
public record OrderApproval(OrderApprovalId id, BusinessId businessId, OrderId orderId,
                            OrderApprovalStatus status, List<String> failureMessages) {
    public OrderApproval { failureMessages = List.copyOf(failureMessages); }
}
