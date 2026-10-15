package kh.edu.istad.platform.customer.restapi.mapper;

import kh.edu.istad.platform.customer.domain.dto.UpdateCustomerCommand;
import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.event.CustomerDeactivatedEvent;
import kh.edu.istad.platform.customer.domain.event.CustomerUpdatedEvent;
import kh.edu.istad.platform.customer.restapi.dto.*;
import kh.edu.istad.platform.customer.domain.dto.InitiateCustomerCommand;
import kh.edu.istad.platform.customer.domain.dto.InitiateCustomerResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface CustomerWebMapper {
    InitiateCustomerCommand toCommand(CustomerInitiateRequest request);

    CustomerInitiateResponse toResponse(InitiateCustomerResult result);

    @Mapping(target = "customerId", source = "id.id")
    @Mapping(target = "email", source = "email.value")
    @Mapping(target = "phoneNumber", source = "phoneNumber.value")
    CustomerResponse toResponse(Customer customer);

    @Mapping(target = "customerId", source = "customer.id.id")
    CustomerUpdateResponse toUpdateResponse(CustomerUpdatedEvent customerUpdatedEvent);

    @Mapping(target = "customerId", source = "customerId.id")
    CustomerDeactivateResponse toDeactivateResponse(CustomerDeactivatedEvent customerDeactivatedEvent);

    @Mapping(target = "customerId", source = "customerId")
    UpdateCustomerCommand toUpdateCommand(CustomerUpdateRequest request, UUID customerId);





}
