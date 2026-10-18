package kh.edu.istad.platform.payment.persistence.repository;

import jakarta.persistence.LockModeType;
import kh.edu.istad.platform.payment.persistence.entity.CreditAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CreditAccountJpaRepository extends JpaRepository<CreditAccountEntity, UUID> {


    List<CreditAccountEntity> findByCustomerIdIn(Collection<UUID> customerIds);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select c from CreditAccountEntity c
            where c.customerId = :customerId
            """)
    Optional<CreditAccountEntity> findByCustomerIdForUpdate(
            @Param("customerId") UUID customerId);

    @Modifying
    @Query(value = """
        INSERT INTO payment_credit_accounts
            (id, customer_id, total_credit_amount)
        VALUES (:id, :customerId, :amount)
        ON CONFLICT (customer_id) DO NOTHING
        """, nativeQuery = true)
    int createIfAbsent(
            @Param("id") UUID id,
            @Param("customerId") UUID customerId,
            @Param("amount") BigDecimal amount);
}
