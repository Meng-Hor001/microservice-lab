package kh.edu.istad.platform.restapi.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import kh.edu.istad.platform.iam.application.dto.AccountActivityResult;
import kh.edu.istad.platform.iam.application.dto.AccountSessionResult;
import kh.edu.istad.platform.iam.application.dto.DeactivationResult;
import kh.edu.istad.platform.iam.application.dto.LoginHistoryResult;
import kh.edu.istad.platform.iam.application.usecase.*;
import kh.edu.istad.platform.restapi.dto.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/me")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
@Validated
public class AccountSecurityController {
    private final ListAccountSessionsUseCase listSessions;
    private final RevokeAccountSessionsUseCase revokeSessions;
    private final ListLoginHistoryUseCase loginHistory;
    private final ListAccountActivityUseCase activity;
    private final RequestPasswordResetUseCase passwordReset;
    private final ManageDeactivationUseCase deactivation;

    private UUID userId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }

    @GetMapping("/sessions")
    public List<AccountSessionResult> sessions(@AuthenticationPrincipal Jwt jwt) {
        return listSessions.execute(
                userId(jwt),
                jwt.getClaimAsString("sid")
        );
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> revokeSession(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID sessionId
    ) {
        revokeSessions.revokeOne(userId(jwt), sessionId);
        return ResponseEntity.noContent().build();
    }

    // Matches the current React "sign out other devices" action.
    @DeleteMapping("/sessions")
    public TerminationResponse revokeOtherSessions(
            @AuthenticationPrincipal Jwt jwt
    ) {
        int count = revokeSessions.revokeOthers(
                userId(jwt),
                jwt.getClaimAsString("sid")
        );
        return new TerminationResponse(count);
    }

    // Also includes the current device.
    @DeleteMapping("/sessions/all")
    public TerminationResponse revokeAllSessions(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return new TerminationResponse(
                revokeSessions.revokeAll(userId(jwt))
        );
    }

    @GetMapping("/login-history")
    public PageResponse<LoginHistoryResult> loginHistory(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "all")
            @Pattern(regexp = "all|success|failed") String result,
            @RequestParam(required = false)
            @Min(1) @Max(3650) Integer days,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10")
            @Min(1) @Max(100) int size
    ) {
        return PageResponse.from(loginHistory.execute(
                userId(jwt), result, days, page, size
        ), entry -> entry);
    }

    @GetMapping("/activity")
    public List<AccountActivityResult> activity(@AuthenticationPrincipal Jwt jwt) {
        return activity.execute(userId(jwt));
    }

    @PostMapping("/password/reset")
    public ResponseEntity<Void> requestPasswordReset(
            @AuthenticationPrincipal Jwt jwt
    ) {
        passwordReset.execute(userId(jwt));
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/deactivation")
    public DeactivationResult deactivation(@AuthenticationPrincipal Jwt jwt) {
        // Return 404 if no pending request exists.
        return deactivation.getPending(userId(jwt));
    }

    @PostMapping("/deactivation")
    public DeactivationResult requestDeactivation(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody DeactivationRequest request
    ) {
        return deactivation.request(userId(jwt), request.reason());
    }

    @DeleteMapping("/deactivation")
    public ResponseEntity<Void> cancelDeactivation(
            @AuthenticationPrincipal Jwt jwt
    ) {
        deactivation.cancel(userId(jwt));
        return ResponseEntity.noContent().build();
    }

    public record TerminationResponse(int count) {}

    public record DeactivationRequest(
            @Pattern(
                    regexp = "not_needed|other_account|privacy|left_me|other"
            )
            String reason
    ) {}
}
