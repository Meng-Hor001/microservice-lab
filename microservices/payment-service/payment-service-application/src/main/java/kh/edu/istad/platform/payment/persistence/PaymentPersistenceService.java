package kh.edu.istad.platform.payment.persistence;

import jakarta.transaction.Transactional;
import kh.edu.istad.platform.payment.domain.entity.CreditEntry;
import kh.edu.istad.platform.payment.domain.entity.CreditHistory;
import kh.edu.istad.platform.payment.domain.entity.Payment;
import kh.edu.istad.platform.payment.domain.service.PaymentDomainService;
import kh.edu.istad.platform.payment.domain.service.PaymentOutcome;
import kh.edu.istad.platform.payment.domain.valueobject.*;
import kh.edu.istad.platform.payment.persistence.entity.CreditAccountEntity;
import kh.edu.istad.platform.payment.persistence.entity.CreditHistoryEntity;
import kh.edu.istad.platform.payment.persistence.entity.PaymentEntity;
import kh.edu.istad.platform.payment.persistence.exception.CreditAccountNotFoundException;
import kh.edu.istad.platform.payment.persistence.exception.DuplicatePaymentException;
import kh.edu.istad.platform.payment.persistence.exception.OrderPaymentConflictException;
import kh.edu.istad.platform.payment.persistence.repository.CreditAccountJpaRepository;
import kh.edu.istad.platform.payment.persistence.repository.CreditHistoryJpaRepository;
import kh.edu.istad.platform.payment.persistence.repository.PaymentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentPersistenceService {

    private final CreditAccountJpaRepository creditAccounts;
    private final PaymentJpaRepository payments;
    private final CreditHistoryJpaRepository creditHistory;
    private final PaymentDomainService domainService;

    @Transactional
    public PaymentOutcome process(
            Payment payment,
            ZonedDateTime now) {

        UUID customerId = payment.customerId().value();

        if (payments.existsByOrderId(payment.orderId().value())) {
            throw new DuplicatePaymentException();
        }

        CreditAccountEntity account = creditAccounts
                .findByCustomerIdForUpdate(customerId)
                .orElseThrow(() -> new CreditAccountNotFoundException(customerId));

        CreditEntry creditEntry = new CreditEntry(
                new CreditEntryId(account.getId()),
                new CustomerId(account.getCustomerId()),
                new Money(account.getTotalCreditAmount()));

        PaymentOutcome outcome =
                domainService.process(payment, creditEntry, now);

        if (outcome.payment().status() == PaymentStatus.COMPLETED) {
            int updated = payments.markOrderPaid(
                    outcome.payment().orderId().value(),
                    outcome.payment().customerId().value(),
                    outcome.payment().price().amount()
            );
            if (updated != 1) {
                throw new OrderPaymentConflictException();
            }
        }

        savePayment(outcome.payment());

        account.setTotalCreditAmount(
                outcome.creditEntry().totalCreditAmount().amount());
        creditAccounts.save(account);

        outcome.history().ifPresent(this::saveCreditHistory);

        return outcome;
    }

    @Transactional
    public CreditEntry topUp(UUID customerId, Money amount) {
        amount.requirePositive();

        creditAccounts.createIfAbsent(
                UUID.randomUUID(),
                customerId,
                Money.zero().amount()
        );

        // Lock the account until this transaction finishes.
        CreditAccountEntity account = creditAccounts
                .findByCustomerIdForUpdate(customerId)
                .orElseThrow(() -> new CreditAccountNotFoundException(customerId));

        CreditEntry creditEntry = new CreditEntry(
                new CreditEntryId(account.getId()),
                new CustomerId(account.getCustomerId()),
                new Money(account.getTotalCreditAmount())
        );

        // CreditEntry is immutable, so use the returned object.
        CreditEntry updatedCredit = creditEntry.addCreditAmount(amount);

        account.setTotalCreditAmount(
                updatedCredit.totalCreditAmount().amount()
        );
        creditAccounts.save(account);

        saveCreditHistory(new CreditHistory(
                CreditHistoryId.generate(),
                new CustomerId(customerId),
                amount,
                TransactionType.CREDIT
        ));

        return updatedCredit;
    }

    private void savePayment(Payment payment) {
        PaymentEntity entity = new PaymentEntity();
        entity.setId(payment.id().value());
        entity.setOrderId(payment.orderId().value());
        entity.setCustomerId(payment.customerId().value());
        entity.setAmount(payment.price().amount());
        entity.setStatus(payment.status().name());
        entity.setCreatedAt(payment.createdAt().toOffsetDateTime());

        payments.save(entity);
    }

    private void saveCreditHistory(CreditHistory history) {
        CreditHistoryEntity entity = new CreditHistoryEntity();
        entity.setId(history.id().value());
        entity.setCustomerId(history.customerId().value());
        entity.setAmount(history.amount().amount());
        entity.setTransactionType(history.transactionType().name());
        entity.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        creditHistory.save(entity);
    }

    public Map<UUID, BigDecimal> getCreditBalances(List<UUID> customerIds){
        Map<UUID, BigDecimal> balances = new HashMap<>();
        customerIds.forEach(id -> balances.put(id, Money.zero().amount()));

        if (!customerIds.isEmpty()) {
            creditAccounts.findByCustomerIdIn(customerIds)
                    .forEach(account -> balances.put(
                            account.getCustomerId(),
                            account.getTotalCreditAmount()
                    ));
        }

        return balances;
    }

}