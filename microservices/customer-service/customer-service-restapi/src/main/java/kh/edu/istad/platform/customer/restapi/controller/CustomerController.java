package kh.edu.istad.platform.customer.restapi.controller;

import jakarta.validation.Valid;
import kh.edu.istad.platform.customer.domain.dto.UpdateCustomerCommand;
import kh.edu.istad.platform.customer.domain.event.CustomerDeactivatedEvent;
import kh.edu.istad.platform.customer.domain.usecase.DeactivateCustomerUseCase;
import kh.edu.istad.platform.customer.domain.usecase.UpdateCustomerUseCase;
import kh.edu.istad.platform.customer.restapi.dto.*;
import kh.edu.istad.platform.customer.restapi.mapper.CustomerWebMapper;
import kh.edu.istad.platform.customer.domain.dto.InitiateCustomerResult;
import kh.edu.istad.platform.customer.domain.usecase.InitiateCustomerUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import kh.edu.istad.platform.customer.domain.usecase.GetAllCustomersUseCase;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.web.server.ResponseStatusException;
import org.springdoc.core.annotations.ParameterObject;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customers")
public class CustomerController {

    private final InitiateCustomerUseCase initiateCustomerUseCase;
    private final CustomerWebMapper customerWebMapper;
    private final UpdateCustomerUseCase updateCustomerUseCase;
    private final DeactivateCustomerUseCase deactivateCustomerUseCase;
    private final GetAllCustomersUseCase getAllCustomersUseCase;

    @GetMapping
    public PagedModel<CustomerResponse> getAllCustomers(
            @ParameterObject
            @PageableDefault(page = 0, size = 15, sort = "customerId")
            Pageable pageable
    ){
        var customers = getAllCustomersUseCase.execute(pageable);
        var responses = customers.map(customer -> new CustomerResponse(
                customer.getId().id(),
                customer.getUsername(),
                customer.getFamilyName(),
                customer.getGivenName(),
                customer.getEmail().value(),
                customer.getPhoneNumber().value()
        ));

        return new PagedModel<>(responses);
    }
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public CustomerInitiateResponse initiateResponse(@Valid @RequestBody CustomerInitiateRequest customerInitiateRequest){
        InitiateCustomerResult result = initiateCustomerUseCase.execute(customerWebMapper.toCommand(customerInitiateRequest));

        return customerWebMapper.toResponse(result);
    }

    @PatchMapping("/{customerId}")
    public CustomerUpdateResponse updateCustomer(
            @PathVariable ("customerId") UUID customerId,
            @Valid @RequestBody CustomerUpdateRequest customerUpdateRequest
            ){
        var command = new UpdateCustomerCommand(
                customerId,
                customerUpdateRequest.familyName(),
                customerUpdateRequest.givenName()
        );

        var event = updateCustomerUseCase.execute(command);
        var customer = event.getCustomer();

        return new CustomerUpdateResponse(
                customer.getId().id(),
                customer.getFamilyName(),
                customer.getGivenName()
        );
    }

    @PatchMapping("/{customerId}/deactivate")
    public CustomerDeactivateResponse deactivateResponse(
            @PathVariable("customerId") UUID customerId
    ){
        var event = deactivateCustomerUseCase.execute(customerId);

        return new CustomerDeactivateResponse(
                event.getCustomerId().id(),
                event.getDeactivatedAt()
        );
    }

}
