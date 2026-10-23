package kh.edu.istad.common.domain.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record Money(
        BigDecimal amount
) {
    public boolean isGreaterThanZero() {
        return this.amount != null && this.amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isLessThanZero() {
        return this.amount != null && this.amount.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean isGreaterThan(Money other) {
        return this.amount != null && this.amount.compareTo(other.amount) > 0;
    }

    public Money add (Money money){
        BigDecimal newAmount = this.amount.add(money.amount);
        return new Money(setScale(newAmount));
    }

    private BigDecimal setScale(BigDecimal inputAmount){
        return inputAmount.setScale(2, RoundingMode.HALF_EVEN);
    }

    public Money subtract (Money money){
        BigDecimal newAmount = this.amount.subtract(money.amount);
        return new Money(setScale(newAmount));
    }

    public Money multiply(int multiplier){
        BigDecimal newAmount = this.amount.multiply(BigDecimal.valueOf(multiplier));
        return new Money(setScale(newAmount));
    }

    public static final Money ZERO = new Money(BigDecimal.ZERO);
}
