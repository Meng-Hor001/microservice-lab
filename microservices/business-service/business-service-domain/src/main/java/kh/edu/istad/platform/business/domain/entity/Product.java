package kh.edu.istad.platform.business.domain.entity;

import kh.edu.istad.platform.business.domain.valueobject.*;
import kh.edu.istad.platform.business.domain.exception.DomainException;
public record Product(ProductId productId, String name, Money price, int quantity, boolean available) {
    public Product {
        if (productId == null || name == null || name.isBlank() || price == null || quantity < 0)
            throw new DomainException("Invalid product");
        price.requirePositive();
    }
}
