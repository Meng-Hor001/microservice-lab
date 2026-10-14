package kh.edu.istad.platform.order.application.port.out;


import kh.edu.istad.platform.order.domain.entity.Order;

public interface OrderRepository {
    Order save (Order order);
}
