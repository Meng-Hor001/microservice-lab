package kh.edu.istad.platform.order.restapi.controller;

import jakarta.validation.Valid;
import kh.edu.istad.platform.order.application.port.out.OrderRepository;
import kh.edu.istad.platform.order.domain.exception.DomainException;
import kh.edu.istad.platform.order.restapi.dto.ApprovalRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/internal/orders")
@RequiredArgsConstructor
public class OrderApprovalController {

    private final OrderRepository orderRepository;

    @PostMapping("/{orderId}/approval")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void applyApproval(
            @PathVariable("orderId") UUID orderId,
            @Valid @RequestBody ApprovalRequest request) {

        try {
            orderRepository.applyApproval(
                    orderId,
                    request.approvalId(),
                    request.businessId(),
                    request.status(),
                    request.failureMessages()
            );
        } catch (DomainException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    exception.getMessage(),
                    exception
            );
        }
    }
}
