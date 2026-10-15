package kh.edu.istad.platform.order.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import kh.edu.istad.platform.order.application.port.out.OrderRepository;
import kh.edu.istad.platform.order.domain.entity.Order;
import kh.edu.istad.platform.order.persistence.mapper.OrderPersistenceMapper;
import kh.edu.istad.platform.order.persistence.repository.OrderJpaRepository;



@Repository
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepository {

    private final OrderJpaRepository jpaRepository;
    private final OrderPersistenceMapper orderPersistenceMapper;

    @Override
    public Page<Order> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(orderPersistenceMapper::toDomain);
    }

    @Override
    @Transactional
    public Order save(Order order) {
        var entity = orderPersistenceMapper.toEntity(order);
        jpaRepository.saveAndFlush(entity);
        return order;
    }
}
