package kh.edu.istad.platform.order.application.dto;

import java.util.List;
import java.util.UUID;

public record CreateOrderCommand(
        UUID customerId,
        UUID businessId,
        String street,
        String postalCode,
        String city,
        List<Item> items
) {
}
