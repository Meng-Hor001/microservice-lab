package kh.edu.istad.platform.order.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
@Getter @Setter @Embeddable
public class OrderItemEmbeddable {
    @Column(nullable=false) private UUID itemId;
    @Column(nullable=false) private UUID productId;
    @Column(nullable=false) private String productName;
    @Column(nullable=false) private int quantity;
    @Column(nullable=false, precision=19, scale=2) private BigDecimal price;
    @Column(nullable=false, precision=19, scale=2) private BigDecimal subTotal;
}
