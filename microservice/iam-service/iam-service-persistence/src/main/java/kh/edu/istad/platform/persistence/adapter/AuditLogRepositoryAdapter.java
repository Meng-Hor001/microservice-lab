package kh.edu.istad.platform.persistence.adapter;

import jakarta.persistence.criteria.Predicate;
import kh.edu.istad.platform.iam.application.dto.AuditLogFilterQuery;
import kh.edu.istad.platform.iam.application.dto.AuditLogResult;
import kh.edu.istad.platform.iam.application.dto.PageResult;
import kh.edu.istad.platform.iam.application.dto.RecordAuditLogCommand;
import kh.edu.istad.platform.iam.application.port.out.AuditLogRepository;
import kh.edu.istad.platform.persistence.entity.AuditLogJpaEntity;
import kh.edu.istad.platform.persistence.mapper.AuditLogMapper;
import kh.edu.istad.platform.persistence.repository.AuditLogJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditLogRepositoryAdapter implements AuditLogRepository {
    private final AuditLogJpaRepository repository;
    private final AuditLogMapper mapper;

    @Override
    @Transactional
    public void save(RecordAuditLogCommand command) {
        repository.save(mapper.toNewEntity(command));
    }

    @Override
    public PageResult<AuditLogResult> findAll(AuditLogFilterQuery query) {
        Specification<AuditLogJpaEntity> specification = (root, criteria, cb) -> {
            var predicates = new ArrayList<Predicate>();
            if (query.action() != null) predicates.add(cb.equal(root.get("action"), query.action()));
            if (query.actor() != null) predicates.add(cb.equal(root.get("actor"), query.actor()));
            if (query.targetType() != null) predicates.add(cb.equal(root.get("targetType"), query.targetType()));
            if (query.targetId() != null) predicates.add(cb.equal(root.get("targetId"), query.targetId()));
            if (query.from() != null) predicates.add(cb.greaterThanOrEqualTo(root.get("occurredAt"), query.from()));
            if (query.to() != null) predicates.add(cb.lessThan(root.get("occurredAt"), query.to()));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        var page = repository.findAll(specification,
                PageRequest.of(query.page(), query.size(), Sort.by(Sort.Direction.DESC, "occurredAt", "id")));
        return new PageResult<>(
                page.getContent().stream().map(mapper::toResult).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
