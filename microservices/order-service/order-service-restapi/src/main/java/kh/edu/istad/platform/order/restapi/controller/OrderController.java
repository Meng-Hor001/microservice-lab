package kh.edu.istad.platform.order.restapi.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import kh.edu.istad.platform.order.application.dto.*;
import kh.edu.istad.platform.order.application.usecase.CreateOrderUseCase;
import kh.edu.istad.platform.order.restapi.dto.CreateOrderRequest;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final CreateOrderUseCase useCase;
    public OrderController(CreateOrderUseCase useCase) { this.useCase = useCase; }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public CreateOrderResult create(@Valid @RequestBody CreateOrderRequest request) {
        var address = request.deliveryAddress();
        return useCase.execute(new CreateOrderCommand(request.customerId(), request.businessId(), address.street(),
                address.postalCode(), address.city(), request.items().stream().map(item ->
                    new CreateOrderCommand.Item(item.productId(), item.name(), item.price(), item.quantity())).toList()));
    }
}
