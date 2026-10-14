package kh.edu.istad.platform.order.domain.valueobject;

import kh.edu.istad.platform.order.domain.exception.DomainException;

import java.util.UUID;

public record CustomerId(UUID id) {
    public CustomerId{
        if (id == null){
            throw new DomainException("Customer ID is required");
        }
    }
}
