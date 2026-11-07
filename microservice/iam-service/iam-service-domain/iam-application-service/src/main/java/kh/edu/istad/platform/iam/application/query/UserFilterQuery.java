package kh.edu.istad.platform.iam.application.query;

import java.util.Set;

public record UserFilterQuery(
        String search,
        Boolean enabled,
        int page,
        int size,
        String sort,
        boolean descending
) {
    private static final Set<String> SORT_FIELDS =
            Set.of("username", "email", "createdAt", "updatedAt");

    public UserFilterQuery {
        if (page < 0) {
            throw new IllegalArgumentException("Page cannot be negative");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100"
            );
        }

        search = search == null || search.isBlank()
                ? null
                : search.trim();

        sort = sort == null || sort.isBlank()
                ? "username"
                : sort.trim();

        if (!SORT_FIELDS.contains(sort)) {
            throw new IllegalArgumentException("Unsupported sort field");
        }
    }
}
