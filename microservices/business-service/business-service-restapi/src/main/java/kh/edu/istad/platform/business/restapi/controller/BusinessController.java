package kh.edu.istad.platform.business.restapi.controller;

import jakarta.validation.Valid;
import kh.edu.istad.platform.business.application.usecase.GetAllBusinessUseCase;
import kh.edu.istad.platform.business.application.usecase.ValidateOrderUseCase;
import kh.edu.istad.platform.business.domain.entity.Business;
import kh.edu.istad.platform.business.domain.entity.OrderDetail;
import kh.edu.istad.platform.business.domain.entity.Product;
import kh.edu.istad.platform.business.domain.valueobject.Money;
import kh.edu.istad.platform.business.domain.valueobject.OrderId;
import kh.edu.istad.platform.business.domain.valueobject.ProductId;
import kh.edu.istad.platform.business.restapi.dto.OrderApprovalResponse;
import kh.edu.istad.platform.business.restapi.dto.ValidateOrderRequest;
import kh.edu.istad.platform.business.restapi.mapper.BusinessWebMapper;
import kh.edu.istad.platform.business.application.dto.RegisterBusinessResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import kh.edu.istad.platform.business.application.usecase.RegisterBusinessUseCase;
import kh.edu.istad.platform.business.restapi.dto.RegisterBusinessRequest;

import java.util.UUID;

@RestController
@RequestMapping("/api/businesses")
@RequiredArgsConstructor
public class BusinessController {

    private final RegisterBusinessUseCase registerBusinessUseCase;
    private final BusinessWebMapper businessWebMapper;
    private final GetAllBusinessUseCase getAllBusinessUseCase;
    private final ValidateOrderUseCase validateOrderUseCase;


    @GetMapping
    public Flux<Business> getAll() {
        return getAllBusinessUseCase.execute();
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<RegisterBusinessResult> register(@Valid @RequestBody RegisterBusinessRequest request) {
        return registerBusinessUseCase.execute(businessWebMapper.toCommand(request));
    }

    @PostMapping("/{businessId}/order-validations")
    public Mono<OrderApprovalResponse> validateOrder(
            @PathVariable("businessId") UUID businessId,
            @Valid @RequestBody ValidateOrderRequest request) {

        var products = request.products().stream()
                .map(product -> new Product(
                        new ProductId(product.productId()),
                        product.name(),
                        new Money(product.price()),
                        product.quantity(),
                        true
                ))
                .toList();

        var orderDetail = new OrderDetail(
                new OrderId(request.orderId()),
                request.orderStatus(),
                new Money(request.totalAmount()),
                products
        );

        return validateOrderUseCase.execute(businessId, orderDetail)
                .map(approval -> new OrderApprovalResponse(
                        approval.id().value(),
                        approval.businessId().value(),
                        approval.orderId().value(),
                        approval.status().name(),
                        approval.failureMessages()
                ));
    }
}
