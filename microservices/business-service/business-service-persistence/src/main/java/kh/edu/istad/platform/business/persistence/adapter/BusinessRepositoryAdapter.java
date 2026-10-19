package kh.edu.istad.platform.business.persistence.adapter;

import kh.edu.istad.platform.business.application.port.out.BusinessRepository;
import kh.edu.istad.platform.business.domain.entity.Business;
import kh.edu.istad.platform.business.domain.valueobject.BusinessId;
import kh.edu.istad.platform.business.persistence.mapper.BusinessPersistenceMapper;
import kh.edu.istad.platform.business.persistence.repository.BusinessMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class BusinessRepositoryAdapter implements BusinessRepository {
    private final BusinessMongoRepository businessMongoRepository;
    private final BusinessPersistenceMapper businessPersistenceMapper;

    @Override
    public Mono<Business> save(Business business) {
        return Mono.defer(() -> businessMongoRepository
                .save(businessPersistenceMapper.toDocument(business))
                .map(businessPersistenceMapper::toDomain));
    }

    @Override
    public Flux<Business> findAll() {
        return businessMongoRepository.findAll()
                .map(businessPersistenceMapper::toDomain);
    }

    @Override
    public Mono<Business> findById(UUID businessId) {
        return businessMongoRepository
                .findById(businessId.toString())
                .map(businessPersistenceMapper::toDomain);
    }
}
