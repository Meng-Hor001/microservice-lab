package kh.edu.istad.platform.order.application.port.out;


import kh.edu.istad.platform.order.domain.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface OrderRepository {


    Page<Order> findAll(Pageable pageable);

    Order save (Order order);

    void applyApproval(
            java.util.UUID orderId,
            java.util.UUID approvalId,
            java.util.UUID businessId,
            String status,
            java.util.List<String> failureMessages
    );
}
