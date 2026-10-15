package kh.edu.istad.platform.business.application.usecase;

import kh.edu.istad.platform.business.application.port.out.BusinessRepository;
import kh.edu.istad.platform.business.domain.entity.Business;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class GetAllBusinessUseCase {

    private final BusinessRepository businessRepository;

    public Flux<Business> execute(){
        return businessRepository.findAll();
    }
}
