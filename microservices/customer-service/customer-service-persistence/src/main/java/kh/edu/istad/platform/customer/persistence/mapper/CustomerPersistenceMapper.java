package kh.edu.istad.platform.customer.persistence.mapper;

import kh.edu.istad.common.domain.valueobject.CustomerId;
import kh.edu.istad.platform.customer.domain.dto.InitiateCustomerCommand;
import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.persistence.Document.CustomerDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CustomerPersistenceMapper {

    CustomerDocument toDocument(Customer customer);


    Customer toDomain(CustomerDocument customerDocument);

    default String map(CustomerId value) {
        return value == null ? null : value.id().toString();
    }

    default CustomerId map(String value) {
        return value == null ? null : new CustomerId(UUID.fromString(value));
    }
}
