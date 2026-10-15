package kh.edu.istad.platform.business.persistence.document;

import java.math.BigDecimal;

public record ProductDocument (
        String id,
        String name,
        BigDecimal price,
        int quantity,
        boolean available
){
}
