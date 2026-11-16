package kh.edu.istad.platform.persistence.adapter;

import kh.edu.istad.platform.iam.application.dto.SystemSettingsResult;
import kh.edu.istad.platform.iam.application.dto.UpdateSystemSettingsCommand;
import kh.edu.istad.platform.iam.application.port.out.SystemSettingsRepository;
import kh.edu.istad.platform.persistence.entity.SystemSettingsJpaEntity;
import kh.edu.istad.platform.persistence.mapper.SystemSettingsMapper;
import kh.edu.istad.platform.persistence.repository.SystemSettingsJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SystemSettingsRepositoryAdapter implements SystemSettingsRepository {
    private final SystemSettingsJpaRepository repository;
    private final SystemSettingsMapper mapper;

    @Override
    public SystemSettingsResult get() {
        return mapper.toResult(load());
    }

    @Override
    @Transactional
    public SystemSettingsResult update(UpdateSystemSettingsCommand command, String actor, Instant now) {
        var entity = load();
        mapper.updateEntity(command, entity);
        entity.setUpdatedAt(now);
        entity.setUpdatedBy(actor);
        return mapper.toResult(repository.saveAndFlush(entity));
    }

    private SystemSettingsJpaEntity load() {
        return repository.findById(SystemSettingsJpaEntity.SINGLETON_ID)
                .orElseThrow(() -> new IllegalStateException("system_settings row is missing; run migration 014"));
    }
}
