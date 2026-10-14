package kh.edu.istad.platform.business.domain.event;

import java.time.ZonedDateTime;
import java.util.List;
import kh.edu.istad.platform.business.domain.entity.OrderApproval;
import kh.edu.istad.platform.business.domain.valueobject.BusinessId;
public record OrderApprovedEvent(OrderApproval orderApproval, BusinessId businessId, List<String> failureMessages, ZonedDateTime createdAt) {
    public OrderApprovedEvent { failureMessages = List.copyOf(failureMessages); }
}
