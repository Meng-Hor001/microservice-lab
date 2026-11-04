package kh.edu.istad.platform.order.domain.command;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateOrderCommand(
        UUID customerId,
        UUID businessId,
        BigDecimal price,
        List<CommandOrderItem> items,
        CommandOrderAddress address
) {
}
