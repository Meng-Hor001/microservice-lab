package kh.edu.istad.platform.order.restapi.controller;

import jakarta.validation.Valid;
import kh.edu.istad.platform.order.application.usecase.GetAllOrderUseCase;
import kh.edu.istad.platform.order.restapi.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
   private final GetAllOrderUseCase getAllOrderUseCase;

   @GetMapping
   public Page<OrderResponse> getAllOrders(@PageableDefault(size = 10,
   sort = "id",
   direction = Sort.Direction.DESC) Pageable pageable){
       return getAllOrderUseCase.execute(pageable)
               .map(orderWebMapper::orderToOrderResponse);
   }
   @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateOrderResult create(
            @Valid @RequestBody CreateOrderRequest createOrderRequest
   ){
       return createOrderUseCase.execute(orderWebMapper.toCommand(createOrderRequest));
   }
}
