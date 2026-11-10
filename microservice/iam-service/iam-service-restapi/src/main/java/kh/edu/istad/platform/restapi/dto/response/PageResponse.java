package kh.edu.istad.platform.restapi.dto.response;

import kh.edu.istad.platform.iam.application.dto.PageResult;

import java.util.List;
import java.util.function.Function;

public record PageResponse<T>(List<T> content, PageMetadata page) {
    public PageResponse {
        content = List.copyOf(content);
    }

    public static <T, R> PageResponse<R> from(
            PageResult<T> result, Function<? super T, ? extends R> mapper
    ) {
        return new PageResponse<>(
                result.content().stream().<R>map(mapper).toList(),
                new PageMetadata(result.number(), result.size(), result.totalElements(), result.totalPages())
        );
    }

    public record PageMetadata(int number, int size, long totalElements, int totalPages) {
    }
}
