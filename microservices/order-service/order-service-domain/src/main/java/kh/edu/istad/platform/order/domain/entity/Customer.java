package kh.edu.istad.platform.order.domain.entity;

import java.util.Objects;
import kh.edu.istad.platform.order.domain.valueobject.CustomerId;
public record Customer(CustomerId id) { public Customer { Objects.requireNonNull(id); } }
