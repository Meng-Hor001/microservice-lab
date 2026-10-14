package kh.edu.istad.platform.order.domain.valueobject;

import kh.edu.istad.platform.order.domain.exception.DomainException;

import java.util.UUID;

public record OrderItemId(UUID id) {

    public OrderItemId {
        if (id == null){
            throw new DomainException("Order item ID is required");
        }
    }

    public static OrderItemId generate(){
        return new OrderItemId(UUID.randomUUID());
    }
}
