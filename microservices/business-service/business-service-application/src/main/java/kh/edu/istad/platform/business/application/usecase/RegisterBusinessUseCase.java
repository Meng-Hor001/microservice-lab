package kh.edu.istad.platform.business.application.usecase;

import java.util.List;
import kh.edu.istad.platform.business.application.dto.*;
import kh.edu.istad.platform.business.application.port.out.BusinessRepository;
import kh.edu.istad.platform.business.domain.entity.Business;
import kh.edu.istad.platform.business.domain.valueobject.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class RegisterBusinessUseCase {
    private final BusinessRepository repository;
    public RegisterBusinessUseCase(BusinessRepository repository) { this.repository = repository; }
    public Mono<RegisterBusinessResult> execute(RegisterBusinessCommand command) {
        return Mono.defer(() -> {
            Business business = Business.register(new BusinessName(command.legalName(), command.displayName()),
                    new BusinessContact(command.email(), command.phoneNumber()), List.of());
            return repository.save(business).map(saved -> new RegisterBusinessResult(saved.id().value(),
                    saved.name().legalName(), saved.name().displayName(), saved.contact().email(),
                    saved.contact().phoneNumber(), saved.active()));
        });
    }
}
