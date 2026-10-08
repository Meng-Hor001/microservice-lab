package kh.edu.istad.platform.customer.domain.usecase;

import kh.edu.istad.platform.customer.domain.service.CustomerDomainService;
import kh.edu.istad.platform.customer.domain.dto.InitiateCustomerCommand;
import kh.edu.istad.platform.customer.domain.dto.InitiateCustomerResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class InitiateCustomerUseCase {

    private final CustomerDomainService customerDomainService;
    public InitiateCustomerResult execute(InitiateCustomerCommand command){
        log.info("initiatete customer usecase:{}", command);
        // validate by load data from persistence (Output port)
        // invoke domain logic (called domain service)
        // save data into database (output port)
        return new InitiateCustomerResult(UUID.randomUUID());
    }
}
