package kh.edu.istad.platform.iam.application.dto;

import java.util.UUID;

public record ChangePasswordCommand(
        UUID userUuid,
        String currentPassword,
        String newPassword,
        String confirmPassword
){
}
