package kh.edu.istad.platform.order.application.usecase;

import kh.edu.istad.platform.order.application.port.out.OrderRepository;
import kh.edu.istad.platform.order.domain.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class GetAllOrderUseCase {

    private final OrderRepository orderRepository;

    public Page<Order> execute(Pageable pageable){
        return orderRepository.findAll(pageable);
    }
}
