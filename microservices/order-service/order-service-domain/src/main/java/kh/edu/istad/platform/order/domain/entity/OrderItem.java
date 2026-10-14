package kh.edu.istad.platform.order.domain.entity;

import kh.edu.istad.platform.order.domain.valueobject.*;
import kh.edu.istad.platform.order.domain.exception.DomainException;
public record OrderItem(OrderItemId id, OrderId orderId, Product product, int quantity, Money price, Money subTotal) {
    public OrderItem {
        if (id == null || orderId == null || product == null || price == null || subTotal == null || quantity <= 0)
            throw new DomainException("Order item requires product and positive quantity");
        if (!price.equals(product.price()) || !price.multiply(quantity).equals(subTotal))
            throw new DomainException("Item price or subtotal mismatch");
    }
    public static OrderItem create(OrderId orderId, Product product, int quantity) {
        if (product == null || quantity <= 0) throw new DomainException("Invalid order item");
        return new OrderItem(OrderItemId.generate(), orderId, product, quantity, product.price(), product.price().multiply(quantity));
    }
}
