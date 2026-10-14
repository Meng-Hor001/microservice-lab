package kh.edu.istad.platform.order.domain.entity;

import kh.edu.istad.platform.order.domain.exception.DomainException;
import kh.edu.istad.platform.order.domain.valueobject.BusinessId;

import java.util.List;
import java.util.Objects;

public record Business(
        BusinessId businessId,
        List<Product> products,
        Boolean active
) {

    public Business {
        if (businessId == null) {
            throw new DomainException("Restaurant ID is required");
        }

        if (products == null
                || products.stream().anyMatch(Objects::isNull)) {
            throw new DomainException("A valid product list is required");
        }

        if (active == null) {
            throw new DomainException("Restaurant active status is required");
        }

        products = List.copyOf(products);
    }
}
