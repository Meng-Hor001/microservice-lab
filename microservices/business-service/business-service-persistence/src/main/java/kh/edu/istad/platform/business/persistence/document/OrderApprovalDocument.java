package kh.edu.istad.platform.business.persistence.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.UUID;

@Document("order_approvals")
public record OrderApprovalDocument(
        @Id
        String orderId,
        UUID approvalId,
        UUID businessId,
        String status,
        List<String> failureMessages
) {
}
