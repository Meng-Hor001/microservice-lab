package kh.edu.istad.platform.business.domain.event;

import java.time.ZonedDateTime;
import java.util.List;
import kh.edu.istad.platform.business.domain.entity.OrderApproval;
import kh.edu.istad.platform.business.domain.valueobject.BusinessId;
public record OrderRejectedEvent(OrderApproval orderApproval, BusinessId businessId, List<String> failureMessages, ZonedDateTime createdAt) {
    public OrderRejectedEvent { failureMessages = List.copyOf(failureMessages); }
}
