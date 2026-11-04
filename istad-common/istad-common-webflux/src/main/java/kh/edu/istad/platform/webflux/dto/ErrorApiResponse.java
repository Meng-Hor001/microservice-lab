package kh.edu.istad.platform.webflux.dto;

import java.util.Map;

public record ErrorApiResponse(
        String message,
        int statusCode,
        Map<String, Object> errorDetails
) {
}
