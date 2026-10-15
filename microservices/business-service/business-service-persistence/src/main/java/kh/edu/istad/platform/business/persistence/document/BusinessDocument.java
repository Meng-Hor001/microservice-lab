package kh.edu.istad.platform.business.persistence.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.util.List;
@Document("businesses")
public record BusinessDocument(
        @Id
        String id,
        String legalName,
        String displayName,
        String email,
        String phoneNumber,
        boolean active,
        List<ProductDocument> products) {
}
