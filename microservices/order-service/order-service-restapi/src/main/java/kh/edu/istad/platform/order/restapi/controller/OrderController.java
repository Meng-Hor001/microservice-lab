package kh.edu.istad.platform.order.restapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import kh.edu.istad.platform.order.application.dto.*;
import kh.edu.istad.platform.order.application.usecase.CreateOrderUseCase;
import kh.edu.istad.platform.order.restapi.dto.CreateOrderRequest;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
   private final CreateOrderUseCase createOrderUseCase;

   @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateOrderResult create(
            @Valid @RequestBody CreateOrderRequest createOrderRequest
   ){
       var address = createOrderRequest.deliveryAddress();

       var item = createOrderRequest
               .items()
               .stream()
               .map(items -> new Item(
                       items.productId(),
                       items.name(),
                       items.price(),
                       items.quantity()
               ))
               .toList();

       return createOrderUseCase.execute(
               new CreateOrderCommand(
                       createOrderRequest.customerId(),
                       createOrderRequest.businessId(),
                       address.street(),
                       address.postalCode(),
                       address.city(),
                       item
               )
       );
   }
}
