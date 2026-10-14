package kh.edu.istad.platform.order.domain.entity;

import kh.edu.istad.platform.order.domain.exception.DomainException;
import kh.edu.istad.platform.order.domain.valueobject.*;

import java.util.List;
import java.util.Objects;

public record Order(
        OrderId orderId,
        CustomerId customerId,
        BusinessId businessId,
        StreetAddress streetAddress,
        Money price,
        List<OrderItem> items,
        TrackingId trackingId,
        OrderStatus status,
        List<String> failureMessages
) {

    public Order {
        validateState(
                orderId,
                customerId,
                businessId,
                streetAddress,
                price,
                items,
                trackingId,
                status,
                failureMessages
        );

        items = List.copyOf(items);
        failureMessages = List.copyOf(failureMessages);
    }

    public static Order initializeOrder(
            OrderId orderId,
            CustomerId customerId,
            BusinessId businessId,
            StreetAddress deliveryAddress,
            List<OrderItem> items
    ) {
        if (items == null
                || items.isEmpty()
                || items.stream().anyMatch(Objects::isNull)) {
            throw new DomainException(
                    "At least one valid order item is required"
            );
        }

        Money total = items.stream()
                .map(OrderItem::subTotal)
                .reduce(Money.zero(), Money::add);

        return new Order(
                orderId,
                customerId,
                businessId,
                deliveryAddress,
                total,
                items,
                TrackingId.generate(),
                OrderStatus.PENDING,
                List.of()
        );
    }

    public void validateOrder() {
        validateState(
                orderId,
                customerId,
                businessId,
                streetAddress,
                price,
                items,
                trackingId,
                status,
                failureMessages
        );
    }

    public Order pay() {
        requireStatus(OrderStatus.PENDING);
        return withStatus(OrderStatus.PAID, failureMessages);
    }

    public Order approve() {
        requireStatus(OrderStatus.PAID);
        return withStatus(OrderStatus.APPROVED, failureMessages);
    }

    public Order initCancel(List<String> reasons) {
        requireStatus(OrderStatus.PAID);
        return withStatus(OrderStatus.CANCELLING, reasons);
    }

    public Order cancel(List<String> reasons) {
        if (status != OrderStatus.PENDING
                && status != OrderStatus.CANCELLING) {
            throw new DomainException(
                    "Cannot cancel an order with status " + status
            );
        }

        return withStatus(OrderStatus.CANCELLED, reasons);
    }

    public Order initCancel() {
        return initCancel(failureMessages);
    }

    public Order cancel() {
        return cancel(failureMessages);
    }

    private void requireStatus(OrderStatus expected) {
        if (status != expected) {
            throw new DomainException(
                    "Expected order status " + expected
                            + " but found " + status
            );
        }
    }

    private Order withStatus(
            OrderStatus status,
            List<String> reasons
    ) {
        return new Order(
                orderId,
                customerId,
                businessId,
                streetAddress,
                price,
                items,
                trackingId,
                status,
                reasons
        );
    }

    private static void validateState(
            OrderId orderId,
            CustomerId customerId,
            BusinessId businessId,
            StreetAddress streetAddress,
            Money price,
            List<OrderItem> items,
            TrackingId trackingId,
            OrderStatus status,
            List<String> failureMessages
    ) {
        if (orderId == null
                || customerId == null
                || businessId == null) {
            throw new DomainException(
                    "Order, customer, and business IDs are required"
            );
        }

        if (streetAddress == null
                || price == null
                || trackingId == null
                || status == null) {
            throw new DomainException(
                    "Address, price, tracking ID, and status are required"
            );
        }

        if (items == null
                || items.isEmpty()
                || items.stream().anyMatch(Objects::isNull)) {
            throw new DomainException(
                    "At least one valid order item is required"
            );
        }

        if (failureMessages == null
                || failureMessages.stream().anyMatch(Objects::isNull)) {
            throw new DomainException(
                    "Failure messages must be a non-null list without null entries"
            );
        }

        Money total = Money.zero();
        var itemIds = new java.util.HashSet<OrderItemId>();

        for (OrderItem item : items) {
            if (!item.orderId().equals(orderId)) {
                throw new DomainException(
                        "An order item belongs to another order"
                );
            }

            if (!itemIds.add(item.orderItemId())) {
                throw new DomainException("Duplicate order item ID");
            }

            total = total.add(item.subTotal());
        }

        price.requirePositive();

        if (!price.equals(total)) {
            throw new DomainException(
                    "Order price must equal the sum of item subtotals"
            );
        }
    }
}
