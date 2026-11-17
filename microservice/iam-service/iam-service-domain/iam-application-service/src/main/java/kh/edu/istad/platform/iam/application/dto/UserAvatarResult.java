package kh.edu.istad.platform.iam.application.dto;

import java.time.Instant;

public record UserAvatarResult(String contentType, byte[] data, Instant updatedAt) {
}
