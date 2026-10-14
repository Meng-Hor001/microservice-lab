package kh.edu.istad.platform.order.domain.entity;

import kh.edu.istad.platform.order.domain.exception.DomainException;
import kh.edu.istad.platform.order.domain.valueobject.Money;
import kh.edu.istad.platform.order.domain.valueobject.OrderId;
import kh.edu.istad.platform.order.domain.valueobject.OrderItemId;

public record OrderItem(
        OrderItemId orderItemId,
        OrderId orderId,
        Product product,
        Integer quantity,
        Money price,
        Money subTotal
) {
    public OrderItem {
        if (orderItemId == null || orderId == null) {
            throw new DomainException("Order item and order IDs are required");
        }

        if (product == null) {
            throw new DomainException("Product is required");
        }

        if (quantity == null || quantity <= 0) {
            throw new DomainException("Quantity must be greater than zero");
        }

        if (price == null || subTotal == null) {
            throw new DomainException("Price and subtotal are required");
        }

        if (!price.equals(product.price())) {
            throw new DomainException(
                    "Order item price must match the product price"
            );
        }

        if (!subTotal.equals(price.multiply(quantity))) {
            throw new DomainException("Order item subtotal is incorrect");
        }
    }

    public static OrderItem create(
            OrderId orderId,
            Product product,
            int quantity
    ){
        if (product == null) {
            throw new DomainException("Product is required");
        }

        if (quantity <= 0) {
            throw new DomainException("Quantity must be greater than zero");
        }

        return new OrderItem(
                OrderItemId.generate(),
                orderId,
                product,
                quantity,
                product.price(),
                product.price().multiply(quantity)
        );
    }

}
