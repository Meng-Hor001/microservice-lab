package kh.edu.istad.platform.order.persistence.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import kh.edu.istad.platform.order.persistence.entity.OrderEntity;
public interface OrderJpaRepository extends JpaRepository<OrderEntity, UUID> {}
