package kh.edu.istad.platform.order.domain.entity;

import java.util.*;
import kh.edu.istad.platform.order.domain.valueobject.*;
import kh.edu.istad.platform.order.domain.exception.DomainException;

/** Immutable aggregate: transitions return a new state, preserving event snapshots. */
public record Order(OrderId id, CustomerId customerId, BusinessId businessId,
                    StreetAddress deliveryAddress, Money price, List<OrderItem> items,
                    TrackingId trackingId, OrderStatus status, List<String> failureMessages) {
    public Order {
        if (id == null || customerId == null || businessId == null || deliveryAddress == null || price == null ||
                trackingId == null || status == null || items == null || items.isEmpty() || items.stream().anyMatch(Objects::isNull))
            throw new DomainException("Order data and at least one item are required");
        items = List.copyOf(items); failureMessages = List.copyOf(failureMessages);
        Money total = Money.zero();
        Set<ProductId> seen = new HashSet<>();
        for (OrderItem item : items) {
            if (!item.orderId().equals(id)) throw new DomainException("Item belongs to another order");
            if (!seen.add(item.product().id())) throw new DomainException("Duplicate product: combine quantities");
            total = total.add(item.subTotal());
        }
        if (!total.equals(price)) throw new DomainException("Order total mismatch");
    }
    public static Order initialize(OrderId id, CustomerId customer, BusinessId business,
                                   StreetAddress address, List<OrderItem> items) {
        if (items == null || items.isEmpty()) throw new DomainException("Order items are required");
        Money total = items.stream().map(OrderItem::subTotal).reduce(Money.zero(), Money::add);
        return new Order(id, customer, business, address, total, items, TrackingId.generate(), OrderStatus.PENDING, List.of());
    }
    private Order transition(OrderStatus expected, OrderStatus next, List<String> failures) {
        if (status != expected) throw new DomainException("Cannot change " + status + " to " + next);
        return new Order(id, customerId, businessId, deliveryAddress, price, items, trackingId, next, failures);
    }
    public Order pay() { return transition(OrderStatus.PENDING, OrderStatus.PAID, failureMessages); }
    public Order approve() { return transition(OrderStatus.PAID, OrderStatus.APPROVED, failureMessages); }
    public Order initCancel(List<String> failures) { return transition(OrderStatus.PAID, OrderStatus.CANCELLING, failures); }
    public Order cancel(List<String> failures) {
        if (status != OrderStatus.PENDING && status != OrderStatus.CANCELLING)
            throw new DomainException("Order cannot be cancelled from " + status);
        return transition(status, OrderStatus.CANCELLED, failures);
    }
}
