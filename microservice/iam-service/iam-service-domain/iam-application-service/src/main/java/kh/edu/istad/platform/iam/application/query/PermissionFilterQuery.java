package kh.edu.istad.platform.iam.application.query;

import java.util.Set;

public record PermissionFilterQuery (
        String search,
        String resource,
        Boolean active,
        int page,
        int size,
        String sort,
        boolean descending
){
    public PermissionFilterQuery {
        if (page < 0) {
            throw new IllegalArgumentException("Page cannot be negative");
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100"
            );
        }

        search = normalize(search);
        resource = normalize(resource);
        sort = sort == null || sort.isBlank() ? "name" : sort.trim();

        if (!Set.of(
                "name",
                "resource",
                "action",
                "createdAt",
                "updatedAt"
        ).contains(sort)) {
            throw new IllegalArgumentException("Unsupported sort field");
        }
    }

    private static String normalize(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }
}
