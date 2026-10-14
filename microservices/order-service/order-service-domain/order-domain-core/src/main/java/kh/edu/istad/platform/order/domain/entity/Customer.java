package kh.edu.istad.platform.order.domain.entity;

import kh.edu.istad.platform.order.domain.exception.DomainException;
import kh.edu.istad.platform.order.domain.valueobject.CustomerId;

public record Customer(CustomerId customerId) {
    public Customer {
        if (customerId == null){
            throw new DomainException("Customer ID is required");
        }
    }
}
