package kh.edu.istad.platform.order.persistence.adapter;

import kh.edu.istad.platform.order.domain.port.out.repository.BusinessRepository;
import kh.edu.istad.platform.order.persistence.repository.BusinessReactiveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BusinessRepositoryAdapter implements BusinessRepository {

    private final BusinessReactiveRepository businessReactiveRepository;

}
