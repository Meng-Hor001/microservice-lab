package kh.edu.istad.platform.order.persistence.adapter;

import java.util.ArrayList;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import kh.edu.istad.platform.order.application.port.out.OrderRepository;
import kh.edu.istad.platform.order.domain.entity.Order;
import kh.edu.istad.platform.order.persistence.entity.*;
import kh.edu.istad.platform.order.persistence.repository.OrderJpaRepository;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepository {

    private final OrderJpaRepository jpaRepository;

    @Override
    @Transactional
    public Order save(Order order) {
        var entity = new OrderEntity();

        entity.setId(order.orderId().id());
        entity.setCustomerId(order.customerId().id());
        entity.setBusinessId(order.businessId().id());
        entity.setTrackingId(order.trackingId().id());

        var address = order.streetAddress();

        entity.setAddressId(address.id());
        entity.setStreet(order.streetAddress().street());
        entity.setPostalCode(address.postalCode());
        entity.setCity(address.city());

        entity.setStatus(order.status().name());
        entity.setPrice(order.price().amount());
        entity.setFailureMessages(
                new ArrayList<>(order.failureMessages())
        );

        for (var item : order.items()) {
            var row = new OrderItemEmbeddable();

            row.setItemId(item.orderItemId().id());
            row.setProductName(item.product().name());
            row.setQuantity(item.quantity());
            row.setPrice(item.price().amount());
            row.setSubTotal(item.subTotal().amount());

            entity.getItems().add(row);
        }

        jpaRepository.saveAndFlush(entity);
        return order;
    }
}
