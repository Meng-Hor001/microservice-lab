package kh.edu.istad.platform.order.domain.valueobject;

import java.util.UUID;
import kh.edu.istad.platform.order.domain.exception.DomainException;
public record TrackingId(UUID value) {
    public TrackingId { if (value == null) throw new DomainException("TrackingId is required"); }
    public static TrackingId generate() { return new TrackingId(UUID.randomUUID()); }
}
