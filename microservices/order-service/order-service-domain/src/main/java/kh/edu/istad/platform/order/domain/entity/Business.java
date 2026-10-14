package kh.edu.istad.platform.order.domain.entity;

import java.util.List;
import java.util.Objects;
import kh.edu.istad.platform.order.domain.valueobject.BusinessId;
public record Business(BusinessId id, List<Product> products, boolean active) {
    public Business { Objects.requireNonNull(id); products = List.copyOf(products); }
}
