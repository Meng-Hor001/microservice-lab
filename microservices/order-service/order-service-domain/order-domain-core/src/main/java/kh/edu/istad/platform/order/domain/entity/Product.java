package kh.edu.istad.platform.order.domain.entity;

import kh.edu.istad.platform.order.domain.exception.DomainException;
import kh.edu.istad.platform.order.domain.valueobject.Money;

public record Product(
        String name,
        Money price
) {

    public Product {
        if (name == null || name.isBlank()) {
            throw new DomainException("Product name is required");
        }
        if (price == null){
            throw new DomainException("Product price is required");
        }

        name = name.strip();
        price.requirePositive();

    }
}
