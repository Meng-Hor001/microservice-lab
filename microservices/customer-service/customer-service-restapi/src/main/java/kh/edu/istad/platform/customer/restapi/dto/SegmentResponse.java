package kh.edu.istad.platform.customer.restapi.dto;

import kh.edu.istad.platform.customer.domain.valueobject.SegmentName;

public record SegmentResponse(String id, SegmentName name, String description) {}
