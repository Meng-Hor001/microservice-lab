package kh.edu.istad.platform.persistence.adapter;

import kh.edu.istad.platform.iam.application.dto.UserAvatarResult;
import kh.edu.istad.platform.iam.application.port.out.UserAvatarRepository;
import kh.edu.istad.platform.persistence.entity.UserAvatarJpaEntity;
import kh.edu.istad.platform.persistence.repository.UserAvatarJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserAvatarRepositoryAdapter implements UserAvatarRepository {
    private final UserAvatarJpaRepository repository;

    @Override
    @Transactional
    public void save(Long userId, String contentType, byte[] data, Instant now) {
        UserAvatarJpaEntity entity = repository.findById(userId)
                .orElseGet(() -> UserAvatarJpaEntity.forUser(userId));
        entity.setContentType(contentType);
        entity.setData(data);
        entity.setSizeBytes(data.length);
        entity.setUpdatedAt(now);
        repository.save(entity);
    }

    @Override
    public Optional<UserAvatarResult> findByUserUuid(UUID userUuid) {
        return repository.findByUserUuid(userUuid)
                .map(a -> new UserAvatarResult(a.getContentType(), a.getData(), a.getUpdatedAt()));
    }

    @Override
    @Transactional
    public void deleteByUserId(Long userId) {
        repository.deleteById(userId);
    }
}
