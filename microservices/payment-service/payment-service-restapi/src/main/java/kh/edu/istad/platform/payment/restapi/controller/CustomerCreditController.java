package kh.edu.istad.platform.payment.restapi.controller;

import jakarta.validation.Valid;
import kh.edu.istad.platform.payment.domain.valueobject.Money;
import kh.edu.istad.platform.payment.persistence.PaymentPersistenceService;
import kh.edu.istad.platform.payment.restapi.dto.TopUpRequest;
import kh.edu.istad.platform.payment.restapi.dto.TopUpResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerCreditController {
    private final PaymentPersistenceService paymentPersistenceService;


    @GetMapping("/credit-balances")
    public Map<UUID, BigDecimal> getCreditBalances(
            @RequestParam("customerIds") List<UUID> customerIds) {
        return paymentPersistenceService.getCreditBalances(customerIds);
    }

    @PostMapping("/{customerId}/top-ups")
    @ResponseStatus(HttpStatus.CREATED)
    public TopUpResponse topUp(
            @PathVariable("customerId") UUID customerId,
            @Valid @RequestBody TopUpRequest request) {

        var credit = paymentPersistenceService.topUp(
                customerId,
                new Money(request.amount())
        );

        return new TopUpResponse(
                credit.customerId().value(),
                credit.totalCreditAmount().amount()
        );
    }
}
