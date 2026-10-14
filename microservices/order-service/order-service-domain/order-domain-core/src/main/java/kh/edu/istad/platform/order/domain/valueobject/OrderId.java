package kh.edu.istad.platform.order.domain.valueobject;

import kh.edu.istad.platform.order.domain.exception.DomainException;

import java.util.UUID;

public record OrderId(UUID id) {
    public OrderId{
        if (id == null){
            throw new DomainException("Order ID is required");
        }
    }

    public static OrderId generate(){
        return new OrderId(UUID.randomUUID());
    }
}
