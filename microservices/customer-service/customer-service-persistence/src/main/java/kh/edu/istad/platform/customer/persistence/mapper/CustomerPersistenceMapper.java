package kh.edu.istad.platform.customer.persistence.mapper;

import kh.edu.istad.common.domain.valueobject.CustomerId;
import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.valueobject.Email;
import kh.edu.istad.platform.customer.domain.valueobject.PhoneNumber;
import kh.edu.istad.platform.customer.persistence.entity.CustomerEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CustomerPersistenceMapper {

    @Mapping(target = "customerId", source = "id.id")
    @Mapping(target = "email", source = "email.value")
    @Mapping(target = "phoneNumber", source = "phoneNumber.value")
    CustomerEntity toEntity(Customer customer);

    @Mapping(target = "id", source = "customerId")
    void mapToBuilder(
            CustomerEntity entity,
            @org.mapstruct.MappingTarget Customer.Builder builder
    );

    default Customer toDomain(CustomerEntity entity) {
        Customer.Builder builder = Customer.Builder.builder();
        mapToBuilder(entity, builder);
        return builder.build();
    }

    default CustomerId toCustomerId(UUID id) {
        return id == null ? null : new CustomerId(id);
    }

    default Email toEmail(String value) {
        return value == null ? null : new Email(value);
    }

    default PhoneNumber toPhoneNumber(String value) {
        return value == null ? null : new PhoneNumber(value);
    }
}
