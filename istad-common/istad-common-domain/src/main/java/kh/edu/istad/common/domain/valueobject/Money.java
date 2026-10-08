package kh.edu.istad.common.domain.valueobject;

import java.math.BigDecimal;

public record Money(
        BigDecimal amount
) {
    public void isGreaterThanZero() {
        if (!(amount.compareTo(BigDecimal.ZERO) > 0)){
            System.out.println("kh.edu.istad.common.domain.valueobject.Money is not greater than zero");
            throw new RuntimeException("kh.edu.istad.common.domain.valueobject.Money is not greater than zero");
        }
    }
}
