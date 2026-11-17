package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.dto.PageResult;
import kh.edu.istad.platform.iam.application.dto.UserSummary;
import kh.edu.istad.platform.iam.application.port.out.UserRepository;
import kh.edu.istad.platform.iam.application.query.UserFilterQuery;
import kh.edu.istad.platform.iam.domain.entity.User;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ListUserUseCase {
    private final UserRepository users;

    public ListUserUseCase(UserRepository users) {
        this.users = users;
    }

    public PageResult<UserSummary> execute(UserFilterQuery query) {
        Objects.requireNonNull(query, "User filter is required");
        PageResult<User> page = users.findAll(query);

        // One query for the whole page's roles, not one per user.
        List<Long> ids = page.content().stream().map(User::getId).toList();
        Map<Long, List<String>> roles = users.findRoleNames(ids);

        return page.map(user -> UserSummary.from(user, roles.getOrDefault(user.getId(), List.of())));
    }
}
