package kh.edu.istad.platform.payment.persistence;

import kh.edu.istad.platform.payment.domain.entity.CreditEntry;
import kh.edu.istad.platform.payment.domain.entity.Payment;
import kh.edu.istad.platform.payment.domain.service.PaymentDomainService;
import kh.edu.istad.platform.payment.domain.service.PaymentOutcome;
import kh.edu.istad.platform.payment.domain.valueobject.CreditEntryId;
import kh.edu.istad.platform.payment.domain.valueobject.CustomerId;
import kh.edu.istad.platform.payment.domain.valueobject.Money;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;

@Service
public class PaymentPersistenceService {
    private final JdbcTemplate jdbc;
    private final PaymentDomainService domainService = new PaymentDomainService();

    public PaymentPersistenceService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public PaymentOutcome process(Payment payment, Money openingCredit, ZonedDateTime now) {
        UUID customerId = payment.customerId().value();
        UUID creditId = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO payment_credit_accounts (id, customer_id, total_credit_amount)
                VALUES (?, ?, ?)
                ON CONFLICT (customer_id) DO NOTHING
                """, creditId, customerId, openingCredit.amount());

        CreditEntry credit = jdbc.queryForObject("""
                SELECT id, total_credit_amount
                FROM payment_credit_accounts
                WHERE customer_id = ?
                FOR UPDATE
                """, (rs, row) -> new CreditEntry(
                new CreditEntryId(rs.getObject("id", UUID.class)),
                payment.customerId(),
                new Money(rs.getBigDecimal("total_credit_amount"))), customerId);

        PaymentOutcome outcome = domainService.process(payment, credit, now);
        jdbc.update("""
                INSERT INTO payments (id, order_id, customer_id, amount, status, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """, outcome.payment().id().value(), outcome.payment().orderId().value(), customerId,
                outcome.payment().price().amount(), outcome.payment().status().name(),
                outcome.payment().createdAt().toOffsetDateTime());
        jdbc.update("UPDATE payment_credit_accounts SET total_credit_amount = ? WHERE customer_id = ?",
                outcome.creditEntry().totalCreditAmount().amount(), customerId);

        outcome.history().ifPresent(history -> jdbc.update("""
                INSERT INTO payment_credit_history (id, customer_id, amount, transaction_type, created_at)
                VALUES (?, ?, ?, ?, ?)
                """, history.id().value(), customerId, history.amount().amount(),
                history.transactionType().name(), now.toOffsetDateTime()));

        return outcome;
    }
}
