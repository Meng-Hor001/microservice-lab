package kh.edu.istad.platform.customer.persistence.mapper;

import kh.edu.istad.platform.customer.domain.entity.CustomerSegment;
import kh.edu.istad.platform.customer.persistence.Document.CustomerSegmentDocument;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SegmentPersistenceMapper {
    CustomerSegmentDocument toDocument(CustomerSegment segment);

    CustomerSegment toDomain(CustomerSegmentDocument segment);
}
