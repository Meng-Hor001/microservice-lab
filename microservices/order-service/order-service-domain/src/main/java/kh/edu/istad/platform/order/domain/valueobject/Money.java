package kh.edu.istad.platform.order.domain.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import kh.edu.istad.platform.order.domain.exception.DomainException;

public record Money(BigDecimal amount) {
    public Money {
        if (amount == null || amount.signum() < 0) throw new DomainException("Money must be nonnegative");
        try { amount = amount.setScale(2, RoundingMode.UNNECESSARY); }
        catch (ArithmeticException ex) { throw new DomainException("Money supports at most two decimal places"); }
        if (amount.precision() > 19) throw new DomainException("Money exceeds supported precision");
    }
    public static Money zero() { return new Money(BigDecimal.ZERO); }
    public Money add(Money other) { return new Money(amount.add(other.amount)); }
    public Money subtract(Money other) { return new Money(amount.subtract(other.amount)); }
    public Money multiply(int quantity) { return new Money(amount.multiply(BigDecimal.valueOf(quantity))); }
    public boolean covers(Money other) { return amount.compareTo(other.amount) >= 0; }
    public void requirePositive() { if (amount.signum() == 0) throw new DomainException("Amount must be positive"); }
}
