package kh.edu.istad.platform.order.restapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import kh.edu.istad.platform.order.application.dto.CreateOrderResult;
import kh.edu.istad.platform.order.restapi.mapper.OrderWebMapper;
import kh.edu.istad.platform.order.application.usecase.CreateOrderUseCase;
import kh.edu.istad.platform.order.restapi.dto.CreateOrderRequest;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
   private final CreateOrderUseCase createOrderUseCase;
   private final OrderWebMapper orderWebMapper;

   @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateOrderResult create(
            @Valid @RequestBody CreateOrderRequest createOrderRequest
   ){
       return createOrderUseCase.execute(orderWebMapper.toCommand(createOrderRequest));
   }
}
