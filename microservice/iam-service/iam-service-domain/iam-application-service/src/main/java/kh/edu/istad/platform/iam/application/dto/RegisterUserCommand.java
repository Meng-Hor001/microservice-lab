package kh.edu.istad.platform.iam.application.dto;

public record RegisterUserCommand(
        String username,
        String email,
        String password,
        String confirmPassword,
        String givenName,
        String familyName,
        String locale
) {
}
