package kh.edu.istad.platform.customer.domain.usecase;

import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.port.out.CustomerRepository;
import kh.edu.istad.platform.customer.domain.service.CustomerDomainService;
import kh.edu.istad.platform.customer.domain.dto.InitiateCustomerCommand;
import kh.edu.istad.platform.customer.domain.dto.InitiateCustomerResult;
import kh.edu.istad.platform.customer.domain.valueobject.Email;
import kh.edu.istad.platform.customer.domain.valueobject.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class InitiateCustomerUseCase {

    private final CustomerDomainService customerDomainService;
    private final CustomerRepository customerRepository;
    public InitiateCustomerResult execute(InitiateCustomerCommand command){
        log.info("initiatete customer usecase:{}", command);
        // validate by load data from persistence (Output port)

        // TODO : Note for my self
        //1. Build the domain customer from the request data
        Customer customer = Customer.Builder.builder()
                .username(command.username())
                .familyName(command.familyName())
                .givenName(command.givenName())
                .email(new Email(command.email()))
                .phoneNumber(new PhoneNumber(command.phoneNumber()))
                .build();

        // 2. Generate the Customer ID and set status to ACTIVE
        customerDomainService.initiateCustomer(customer);


        // 3. Store the customer in the in memory-map Map that has been created already
        Customer saveCustomer = customerRepository.save(customer);


        // invoke domain logic (called domain service)
        // save data into database (output port)
        return new InitiateCustomerResult(
                saveCustomer.getId().id(),
                saveCustomer.getUsername(),
                saveCustomer.getFamilyName(),
                saveCustomer.getGivenName(),
                saveCustomer.getEmail().value(),
                saveCustomer.getPhoneNumber().value()
        );
    }
}
