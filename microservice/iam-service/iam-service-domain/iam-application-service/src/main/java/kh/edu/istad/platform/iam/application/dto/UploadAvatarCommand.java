package kh.edu.istad.platform.iam.application.dto;

import java.util.UUID;

public record UploadAvatarCommand(UUID userUuid, String contentType, byte[] data) {
}
