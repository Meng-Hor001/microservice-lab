package kh.edu.istad.platform.iam.application.dto;

public record CreatePermissionCommand(
        String resource,
        String action,
        String description) {
}