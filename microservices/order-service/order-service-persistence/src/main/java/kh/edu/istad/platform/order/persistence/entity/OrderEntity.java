package kh.edu.istad.platform.order.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.*;
import lombok.Getter;
import lombok.Setter;
@Getter @Setter @Entity @Table(name="orders")
public class OrderEntity {
    @Id private UUID id;
    @Column(nullable=false) private UUID customerId;
    @Column(nullable=false) private UUID businessId;
    @Column(nullable=false, unique=true) private UUID trackingId;
    @Column(nullable=false) private UUID addressId;
    @Column(nullable=false) private String street;
    @Column(nullable=false) private String postalCode;
    @Column(nullable=false) private String city;
    @Column(nullable=false) private String status;
    @Column(nullable=false, precision=19, scale=2) private BigDecimal price;
    @ElementCollection
    @CollectionTable(name="order_items", joinColumns=@JoinColumn(name="order_id"))
    @OrderColumn(name="item_position")
    private List<OrderItemEmbeddable> items = new ArrayList<>();
    @ElementCollection
    @CollectionTable(name="order_failures", joinColumns=@JoinColumn(name="order_id"))
    @Column(name="message")
    private List<String> failureMessages = new ArrayList<>();
}
