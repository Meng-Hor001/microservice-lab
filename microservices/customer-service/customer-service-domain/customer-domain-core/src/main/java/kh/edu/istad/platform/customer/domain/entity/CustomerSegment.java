package kh.edu.istad.platform.customer.domain.entity;

import kh.edu.istad.common.domain.exception.CustomerDomainException;
import kh.edu.istad.platform.customer.domain.valueobject.SegmentName;

public record CustomerSegment(
        String id,
        SegmentName name,
        String description
) {
    public CustomerSegment{
        if (name == null){
            throw new CustomerDomainException("Segment name is required");
        }

        if (description == null){
            description = "";
        }else {
            description = description.trim();
        }
    }
}
