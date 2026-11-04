package kh.edu.istad.platform.order.persistence.repository;

import kh.edu.istad.platform.order.persistence.entity.BusinessEntity;
import kh.edu.istad.platform.order.persistence.entity.BusinessEntityId;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

public interface BusinessReactiveRepository extends R2dbcRepository<BusinessEntity, BusinessEntityId> {
}
