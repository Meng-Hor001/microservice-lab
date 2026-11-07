package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.dto.PageResult;
import kh.edu.istad.platform.iam.application.dto.UserSummary;
import kh.edu.istad.platform.iam.application.port.out.UserRepository;
import kh.edu.istad.platform.iam.application.query.UserFilterQuery;

import java.util.Objects;

public final class ListUserUseCase {
    private final UserRepository users;

    public ListUserUseCase(UserRepository users) {
        this.users = users;
    }


    public PageResult<UserSummary> execute(UserFilterQuery query) {
        Objects.requireNonNull(query, "User filter is required");
        return users.findAll(query).map(UserSummary::from);
    }
}
