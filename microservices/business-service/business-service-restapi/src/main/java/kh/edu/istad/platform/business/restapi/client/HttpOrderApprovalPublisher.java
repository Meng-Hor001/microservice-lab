package kh.edu.istad.platform.business.restapi.client;

import jakarta.validation.Valid;
import kh.edu.istad.platform.business.application.port.out.OrderApprovalPublisher;
import kh.edu.istad.platform.business.domain.entity.OrderApproval;
import kh.edu.istad.platform.business.domain.event.OrderApprovedEvent;
import kh.edu.istad.platform.business.domain.event.OrderRejectedEvent;
import kh.edu.istad.platform.business.domain.valueobject.OrderApprovalStatus;
import kh.edu.istad.platform.business.restapi.dto.DecisionMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Component
public class HttpOrderApprovalPublisher implements OrderApprovalPublisher {
    private final WebClient webClient;

    public HttpOrderApprovalPublisher(@Value("${order-service.base-url}") String baseUrl){
        webClient = WebClient
                .builder()
                .baseUrl(baseUrl)
                .build();
    }
    @Override
    public Mono<Void> publish(OrderApproval orderApproval) {
        return Mono.defer(() -> {
            var now = ZonedDateTime.now(ZoneOffset.UTC);
            DecisionMessage message;

            if (orderApproval.status() == OrderApprovalStatus.APPROVED) {
                var event = new OrderApprovedEvent(
                        orderApproval,
                        orderApproval.businessId(),
                        orderApproval.failureMessages(),
                        now
                );

                message = toMessage(event.orderApproval());
            } else {
                var event = new OrderRejectedEvent(
                        orderApproval,
                        orderApproval.businessId(),
                        orderApproval.failureMessages(),
                        now
                );
                message = toMessage(event.orderApproval());
            }
            return webClient.post()
                    .uri("/internal/orders/{orderId}/approval",
                            orderApproval.orderId().value())
                    .bodyValue(message)
                    .retrieve()
                    .toBodilessEntity()
                    .then()
                    .timeout(Duration.ofSeconds(5))
                    .onErrorMap(error -> new ResponseStatusException(
                            HttpStatus.BAD_GATEWAY,
                            "Decision saved, but delivery failed. "
                                    + "Retry the same request to resend it.",
                            error
                    ));
        });
    }

    private DecisionMessage toMessage(OrderApproval approval) {
        return new DecisionMessage(
                approval.id().value(),
                approval.businessId().value(),
                approval.status().name(),
                approval.failureMessages()
        );
    }
}
