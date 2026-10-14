package kh.edu.istad.platform.order.persistence.adapter;

import java.util.ArrayList;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import kh.edu.istad.platform.order.application.port.out.OrderRepository;
import kh.edu.istad.platform.order.domain.entity.Order;
import kh.edu.istad.platform.order.persistence.entity.*;
import kh.edu.istad.platform.order.persistence.repository.OrderJpaRepository;

@Repository
public class OrderRepositoryAdapter implements OrderRepository {
    private final OrderJpaRepository repository;
    public OrderRepositoryAdapter(OrderJpaRepository repository) { this.repository = repository; }
    @Override @Transactional
    public Order save(Order order) {
        var entity = new OrderEntity();
        entity.setId(order.id().value()); entity.setCustomerId(order.customerId().value());
        entity.setBusinessId(order.businessId().value()); entity.setTrackingId(order.trackingId().value());
        entity.setAddressId(order.deliveryAddress().id()); entity.setStreet(order.deliveryAddress().street());
        entity.setPostalCode(order.deliveryAddress().postalCode()); entity.setCity(order.deliveryAddress().city());
        entity.setStatus(order.status().name()); entity.setPrice(order.price().amount());
        entity.setFailureMessages(new ArrayList<>(order.failureMessages()));
        for (var item : order.items()) {
            var row = new OrderItemEmbeddable();
            row.setItemId(item.id().value()); row.setProductId(item.product().id().value());
            row.setProductName(item.product().name()); row.setQuantity(item.quantity());
            row.setPrice(item.price().amount()); row.setSubTotal(item.subTotal().amount());
            entity.getItems().add(row);
        }
        repository.saveAndFlush(entity);
        return order;
    }
}
