package kh.edu.istad.platform.payment.persistence.repository;

import kh.edu.istad.platform.payment.persistence.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, UUID> {
    boolean existsByOrderId(UUID orderId);

    @Modifying
    @Query(value = """
            UPDATE orders
            SET status = 'PAID'
            WHERE id = :orderId
              AND customer_id = :customerId
              AND price = :amount
              AND status = 'PENDING'
            """, nativeQuery = true)
    int markOrderPaid(
            @Param("orderId") UUID orderId,
            @Param("customerId") UUID customerId,
            @Param("amount") BigDecimal amount
    );
}
