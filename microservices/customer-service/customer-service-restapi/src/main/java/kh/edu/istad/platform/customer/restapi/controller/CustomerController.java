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
    private final GetAllCustomersUseCase getAllCustomersUseCase;
    private final DeactivateCustomerUseCase deactivateCustomerUseCase;

    @GetMapping
    public PagedModel<CustomerResponse> getAllCustomers(
            @ParameterObject @PageableDefault(size = 10, sort = "customerId") Pageable pageable
    ) {
        try {
            var customers = getAllCustomersUseCase.execute(pageable);
            return new PagedModel<>(customers.map(customer -> new CustomerResponse(
                    customer.getId().id(),
                    customer.getUsername(),
                    customer.getFamilyName(),
                    customer.getGivenName(),
                    customer.getEmail() == null ? null : customer.getEmail().value(),
                    customer.getPhoneNumber() == null ? null : customer.getPhoneNumber().value()
            )));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public CustomerInitiateResponse initiateResponse(@RequestBody CustomerInitiateRequest customerInitiateRequest){
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
