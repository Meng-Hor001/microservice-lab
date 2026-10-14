package kh.edu.istad.platform.order.domain.valueobject;

import kh.edu.istad.platform.order.domain.exception.DomainException;

import java.util.UUID;

public record BusinessId(UUID id) {
    public BusinessId{
        if (id == null){
            throw new DomainException("Business ID is required");
        }
    }
}
