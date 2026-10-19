package kh.edu.istad.platform.order.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name="orders")
public class OrderEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID customerId;

    @Column(nullable = false)
    private UUID businessId;

    @Column(name = "business_approval_id")
    private UUID businessApprovalId;

    @Column(nullable = false,unique = true)
    private UUID trackingId;

    @Column(nullable = false)
    private UUID addressId;

    @Column(nullable = false, length = 255)
    private String street;

    @Column(nullable = false, length = 30)
    private String postalCode;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @ElementCollection
    @CollectionTable(
            name = "order_items",
            joinColumns = @JoinColumn(name = "order_id")
    )
    @OrderColumn(name = "item_position")
    private List<OrderItemEmbeddable> items = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name = "order_failure_messages",
            joinColumns = @JoinColumn(name = "order_id")
    )
    @Column(name = "message", length = 1000)
    private List<String> failureMessages = new ArrayList<>();
}
