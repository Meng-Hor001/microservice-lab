package kh.edu.istad.platform.customer.restapi.mapper;

import kh.edu.istad.platform.customer.domain.entity.CustomerSegment;
import kh.edu.istad.platform.customer.restapi.dto.SegmentResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SegmentWebMapper {
    SegmentResponse toResponse(CustomerSegment segment);
}
