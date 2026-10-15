package kh.edu.istad.platform.business.application.usecase;

import java.util.List;

import kh.edu.istad.platform.business.application.dto.RegisterBusinessCommand;
import kh.edu.istad.platform.business.application.dto.RegisterBusinessResult;
import kh.edu.istad.platform.business.application.mapper.BusinessApplicationMapper;
import kh.edu.istad.platform.business.application.port.out.BusinessRepository;
import kh.edu.istad.platform.business.domain.entity.Business;
import kh.edu.istad.platform.business.domain.exception.DomainException;
import kh.edu.istad.platform.business.domain.valueobject.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RegisterBusinessUseCase {
    private final BusinessRepository businessRepository;
    private final BusinessApplicationMapper businessApplicationMapper;

    public Mono<RegisterBusinessResult> execute(RegisterBusinessCommand command){
        return Mono.defer(() -> {
            if (command == null) {
                throw new DomainException("Business command is required");
            }

            var business = Business.register(
                    new BusinessName(
                            command.legalName(),
                            command.displayName()
                    ),
                    new BusinessContact(
                            command.email(),
                            command.phoneNumber()
                    ),
                    List.of()
            );

            return businessRepository.save(business)
                    .map(businessApplicationMapper::toResult);
        });
    }

}
