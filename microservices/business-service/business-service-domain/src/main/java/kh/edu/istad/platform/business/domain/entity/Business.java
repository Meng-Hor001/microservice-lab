package kh.edu.istad.platform.business.domain.entity;

import java.util.*;
import kh.edu.istad.platform.business.domain.valueobject.*;
import kh.edu.istad.platform.business.domain.exception.DomainException;

/** Aggregate root. Product quantities in the catalog represent available stock. */
public record Business(BusinessId id, BusinessName name, BusinessContact contact,
                       boolean active, List<Product> products) {
    public Business {
        if (id == null || name == null || contact == null || products == null || products.stream().anyMatch(Objects::isNull))
            throw new DomainException("Business data is required");
        products = List.copyOf(products);
        if (products.stream().map(Product::productId).distinct().count() != products.size())
            throw new DomainException("Duplicate catalog product");
    }
    public static Business register(BusinessName name, BusinessContact contact, List<Product> products) {
        return new Business(BusinessId.generate(), name, contact, true, products);
    }
    public OrderApproval approve(OrderDetail order) {
        if (order == null) throw new DomainException("Order is required");
        List<String> failures = new ArrayList<>();
        if (!active) failures.add("Business is inactive");
        if (order.orderStatus() != OrderStatus.PAID) failures.add("Order must be paid");
        Map<ProductId, Product> catalog = new HashMap<>();
        products.forEach(p -> catalog.put(p.productId(), p));
        Set<ProductId> seen = new HashSet<>();
        Money total = Money.zero();
        for (Product requested : order.products()) {
            if (!seen.add(requested.productId())) failures.add("Duplicate order product");
            if (requested.quantity() <= 0) failures.add("Quantity must be positive");
            Product actual = catalog.get(requested.productId());
            if (actual == null || !actual.available()) failures.add("Product unavailable: " + requested.productId().value());
            else {
                if (actual.quantity() < requested.quantity()) failures.add("Insufficient stock");
                if (!actual.price().equals(requested.price())) failures.add("Product price mismatch");
            }
            total = total.add(requested.price().multiply(requested.quantity()));
        }
        if (!total.equals(order.totalAmount())) failures.add("Order total mismatch");
        return new OrderApproval(OrderApprovalId.generate(), id, order.orderId(),
                failures.isEmpty() ? OrderApprovalStatus.APPROVED : OrderApprovalStatus.REJECTED, failures);
    }
}
