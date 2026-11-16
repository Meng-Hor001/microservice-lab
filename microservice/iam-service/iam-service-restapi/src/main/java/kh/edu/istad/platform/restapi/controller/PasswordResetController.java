package kh.edu.istad.platform.restapi.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import kh.edu.istad.platform.iam.application.usecase.ConfirmPasswordResetUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/password/reset")
@RequiredArgsConstructor
public class PasswordResetController {
    private final ConfirmPasswordResetUseCase confirmReset;

    @PostMapping("/confirm")
    public ResponseEntity<Void> confirm(@Valid @RequestBody Confirmation request) {
        confirmReset.execute(request.token(), request.newPassword(), request.confirmPassword());
        return ResponseEntity.noContent().build();
    }

    public record Confirmation(
            @NotBlank @Size(max = 100) String token,
            @NotBlank @Size(min = 8, max = 64) String newPassword,
            @NotBlank @Size(min = 8, max = 64) String confirmPassword
    ) {
    }
}
