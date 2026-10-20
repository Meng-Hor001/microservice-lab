package kh.edu.istad.platform.customer.restapi.mapper;

import kh.edu.istad.platform.customer.domain.dto.CreateCustomerResult;
import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.valueobject.Email;
import kh.edu.istad.platform.customer.domain.valueobject.PhoneNumber;
import kh.edu.istad.platform.customer.restapi.dto.CreateCustomerRequest;
import kh.edu.istad.platform.customer.restapi.dto.CustomerInitiateResponse;
import kh.edu.istad.platform.customer.domain.dto.InitiateCustomerCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring" )
public interface CustomerWebMapper {
    InitiateCustomerCommand toCommand(CreateCustomerRequest request);

    CustomerInitiateResponse toResponse(CreateCustomerResult result);

    @Mapping(source = "phoneNumber", target = "phoneNumber.value")
    @Mapping(source = "email", target = "email.value")
    Customer toDomain (CreateCustomerRequest request);

    default Email toEmail(String email){
        return new Email(email);
    }

    default PhoneNumber toPhoneNumber(String phoneNumber){
        return new PhoneNumber(phoneNumber);
    }
}
