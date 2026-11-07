package kh.edu.istad.platform.iam.application.dto;

import java.util.List;
import java.util.function.Function;

public record PageResult<T>(
        List<T> content,
        int number,
        int size,
        long totalElements,
        int totalPages
) {

    public PageResult {
        content = List.copyOf(content);

        if (number < 0 || size < 1
                || totalElements < 0 || totalPages < 0) {
            throw new IllegalArgumentException("Invalid page information");
        }
    }

    public <R> PageResult<R> map(Function<? super T, ? extends R> mapper) {
        List<R> mapped = content.stream()
                .<R>map(mapper)
                .toList();

        return new PageResult<>(
                mapped,
                number,
                size,
                totalElements,
                totalPages
        );
    }
}
