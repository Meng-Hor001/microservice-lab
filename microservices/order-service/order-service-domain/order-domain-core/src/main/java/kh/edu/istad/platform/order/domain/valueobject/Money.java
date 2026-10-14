package kh.edu.istad.platform.order.domain.valueobject;

import kh.edu.istad.platform.order.domain.exception.DomainException;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record Money(BigDecimal amount) {
    public Money {
        if (amount == null){
            throw new DomainException("Amount is required");
        }

        if (amount.signum() < 0 ){
            throw new DomainException("Amount cannot be negative");
        }

        try {
            amount = amount.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException e) {
            throw new DomainException("Amount supports at most two decimal places");
        }

        if (amount.precision() > 19){
            throw new DomainException("Amount exceed supported precision");
        }
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }

    public Money add(Money other) {
        validateOther(other);
        return new Money(amount.add(other.amount()));
    }

    public Money subtract(Money other) {
        validateOther(other);

        if (amount.compareTo(other.amount()) < 0) {
            throw new DomainException("Resulting amount cannot be negative");
        }

        return new Money(amount.subtract(other.amount()));
    }

    public Money multiply(int quantity) {
        if (quantity < 0) {
            throw new DomainException("Quantity cannot be negative");
        }

        return new Money(
                amount.multiply(BigDecimal.valueOf(quantity))
        );
    }

    public boolean covers(Money other) {
        validateOther(other);
        return amount.compareTo(other.amount()) >= 0;
    }

    public void requirePositive() {
        if (amount.signum() <= 0) {
            throw new DomainException("Amount must be positive");
        }
    }

    private static void validateOther(Money other) {
        if (other == null) {
            throw new DomainException("Money operand is required");
        }
    }
}
