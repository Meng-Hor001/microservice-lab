package kh.edu.istad.platform.order.domain.valueobject;

import kh.edu.istad.platform.order.domain.exception.DomainException;

import java.util.UUID;

public record TrackingId(UUID id) {
    public TrackingId{
        if (id == null){
            throw new DomainException("Tracking ID is required");
        }
    }
}
