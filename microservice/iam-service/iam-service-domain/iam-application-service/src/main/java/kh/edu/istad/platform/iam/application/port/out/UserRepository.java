package kh.edu.istad.platform.iam.application.port.out;

import kh.edu.istad.platform.iam.application.dto.PageResult;
import kh.edu.istad.platform.iam.application.query.UserFilterQuery;
import kh.edu.istad.platform.iam.domain.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByUuid(UUID uuid);

    Optional<User> findById(Long id);

    Optional<User> findByUsernameOrEmail(String usernameOrEmail);

    PageResult<User> findAll(UserFilterQuery query);

    User save(User user);
}
