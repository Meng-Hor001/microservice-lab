package kh.edu.istad.platform.order.domain.command;

import kh.edu.istad.platform.order.domain.port.out.repository.BusinessRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class CreateOrderCommandHandler {

    private final BusinessRepository businessRepository;

    public Mono<CreateOrderResult> handle(CreateOrderCommand command) {
        log.info("Received CreateOrderCommand {}", command);
        return Mono.empty();
    }

}
