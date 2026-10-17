package kh.edu.istad.platform.payment.persistence;

import jakarta.transaction.Transactional;
import kh.edu.istad.platform.payment.domain.entity.CreditEntry;
import kh.edu.istad.platform.payment.domain.entity.CreditHistory;
import kh.edu.istad.platform.payment.domain.entity.Payment;
import kh.edu.istad.platform.payment.domain.service.PaymentDomainService;
import kh.edu.istad.platform.payment.domain.service.PaymentOutcome;
import kh.edu.istad.platform.payment.domain.valueobject.CreditEntryId;
import kh.edu.istad.platform.payment.domain.valueobject.CustomerId;
import kh.edu.istad.platform.payment.domain.valueobject.Money;
import kh.edu.istad.platform.payment.persistence.entity.CreditAccountEntity;
import kh.edu.istad.platform.payment.persistence.entity.CreditHistoryEntity;
import kh.edu.istad.platform.payment.persistence.entity.PaymentEntity;
import kh.edu.istad.platform.payment.persistence.exception.DuplicatePaymentException;
import kh.edu.istad.platform.payment.persistence.repository.CreditAccountJpaRepository;
import kh.edu.istad.platform.payment.persistence.repository.CreditHistoryJpaRepository;
import kh.edu.istad.platform.payment.persistence.repository.PaymentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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
            Money openingBalance,
            ZonedDateTime now) {

        UUID customerId = payment.customerId().value();

        if (payments.existsByOrderId(payment.orderId().value())) {
            throw new DuplicatePaymentException();
        }


        // Create an account only when this customer has none.
        creditAccounts.createIfAbsent(
                UUID.randomUUID(),
                customerId,
                openingBalance.amount());

        CreditAccountEntity account = creditAccounts
                .findByCustomerIdForUpdate(customerId)
                .orElseThrow(() -> new IllegalStateException(
                        "Could not load customer credit account"));

        CreditEntry creditEntry = new CreditEntry(
                new CreditEntryId(account.getId()),
                new CustomerId(account.getCustomerId()),
                new Money(account.getTotalCreditAmount()));

        PaymentOutcome outcome =
                domainService.process(payment, creditEntry, now);

        savePayment(outcome.payment());

        account.setTotalCreditAmount(
                outcome.creditEntry().totalCreditAmount().amount());
        creditAccounts.save(account);

        outcome.history().ifPresent(this::saveCreditHistory);

        return outcome;
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

}