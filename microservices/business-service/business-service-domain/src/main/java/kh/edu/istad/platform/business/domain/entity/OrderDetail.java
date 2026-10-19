package kh.edu.istad.platform.business.domain.entity;

import java.util.List;
import kh.edu.istad.platform.business.domain.valueobject.*;
import kh.edu.istad.platform.business.domain.exception.DomainException;
public record OrderDetail(
        OrderId orderId,
        OrderStatus orderStatus,
        Money totalAmount,
        List<Product> products) {
    public OrderDetail {
        if (orderId == null || orderStatus == null || totalAmount == null || products == null || products.isEmpty() || products.stream().anyMatch(p -> p == null))
            throw new DomainException("Order details are required");
        products = List.copyOf(products);
    }
}
