package kh.edu.istad.platform.customer.restapi.controller;
import kh.edu.istad.platform.customer.domain.valueobject.CustomerStatus;
import kh.edu.istad.platform.customer.domain.usecase.UpdateCustomerUseCase;
import kh.edu.istad.platform.customer.domain.usecase.DeactivateCustomerUseCase;
import kh.edu.istad.platform.customer.domain.dto.CustomerStatistics;
import java.util.UUID;
import jakarta.validation.Valid;

import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.usecase.QueryCustomersUseCase;
import kh.edu.istad.platform.customer.restapi.dto.*;
import kh.edu.istad.platform.customer.restapi.mapper.CustomerWebMapper;
import kh.edu.istad.platform.customer.domain.usecase.CreateCustomerUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customers")
public class CustomerController {

    private final CreateCustomerUseCase createCustomerUseCase;
    private final CustomerWebMapper customerWebMapper;
    private final QueryCustomersUseCase queryCustomersUseCase;
    private final UpdateCustomerUseCase updates;
    private final DeactivateCustomerUseCase deactivations;

    @GetMapping("/{id}")
    public Mono<CustomerResponse> findById(@PathVariable UUID id) {
        return queryCustomersUseCase.findById(id)
                .map(customerWebMapper::toResponse);
    }

    @PutMapping("/{id}")
    public Mono<CustomerResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody CustomerUpdateRequest request
    ) {
        return updates.update(
                id,
                request.familyName(),
                request.givenName(),
                request.segmentId()
        ).map(customerWebMapper::toResponse);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable UUID id) {
        return queryCustomersUseCase.delete(id);
    }

    @PatchMapping("/{id}/deactivate")
    public Mono<CustomerResponse> deactivate(@PathVariable UUID id) {
        return deactivations.deactivate(id)
                .map(customerWebMapper::toResponse);
    }

    @GetMapping("/statistics")
    public Mono<CustomerStatistics> statistics() {
        return queryCustomersUseCase.statistics();
    }


    @GetMapping
    public Flux<CustomerResponse> findAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) CustomerStatus status,
            @RequestParam(required = false) String segmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return queryCustomersUseCase
                .search(keyword, status, segmentId, page, size)
                .map(customerWebMapper::toResponse);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CustomerResponse> create(
            @Valid @RequestBody CreateCustomerRequest request
    ) {
        return createCustomerUseCase
                .create(customerWebMapper.toDomain(request))
                .map(customerWebMapper::toResponse);
    }
}
