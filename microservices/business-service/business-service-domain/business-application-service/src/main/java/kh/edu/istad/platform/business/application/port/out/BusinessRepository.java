package kh.edu.istad.platform.business.application.port.out;

import kh.edu.istad.platform.business.domain.entity.Business;
import kh.edu.istad.platform.business.domain.valueobject.BusinessId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface BusinessRepository {
    Mono<Business> save(Business business);

    Flux<Business> findAll();

    Mono<Business> findById(UUID businessId);
}
