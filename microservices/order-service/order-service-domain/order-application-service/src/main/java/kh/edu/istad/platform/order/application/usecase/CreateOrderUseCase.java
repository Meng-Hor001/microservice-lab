package kh.edu.istad.platform.order.application.usecase;

import kh.edu.istad.platform.order.application.dto.CreateOrderCommand;
import kh.edu.istad.platform.order.application.dto.CreateOrderResult;
import kh.edu.istad.platform.order.application.port.out.OrderRepository;
import kh.edu.istad.platform.order.domain.entity.Order;
import kh.edu.istad.platform.order.domain.entity.OrderItem;
import kh.edu.istad.platform.order.domain.entity.Product;
import kh.edu.istad.platform.order.domain.exception.DomainException;
import kh.edu.istad.platform.order.domain.valueobject.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateOrderUseCase {
    private final OrderRepository orderRepository;

    @Transactional
    public CreateOrderResult execute(CreateOrderCommand command){
        if (command == null){
            throw  new DomainException("Order command is required");
        }

        if (command.items() == null || command.items().isEmpty()){
            throw new DomainException("At least one order item is required");
        }

        if (command.items().stream().anyMatch(item -> item == null)){
            throw new DomainException("Order items cannot contain null");
        }

        var orderId = OrderId.generate();

        var address = new StreetAddress(
                UUID.randomUUID(),
                command.street(),
                command.postalCode(),
                command.city()
        );

        var items = command
                .items()
                .stream()
                .map(item -> {
                   var product = new Product(
                           item.name(),
                           new Money(item.price())
                   );

                   return OrderItem.create(
                           orderId,
                           product,
                           item.quantity()
                   );
                })
                .toList();

        var order = Order.initializeOrder(
                orderId,
                new CustomerId(command.customerId()),
                new BusinessId(command.businessId()),
                address,
                items
        );

        var saved = orderRepository.save(order);

        return new CreateOrderResult(
                saved.orderId().id(),
                saved.trackingId().id(),
                saved.status(),
                saved.price().amount()
        );
    }
}
