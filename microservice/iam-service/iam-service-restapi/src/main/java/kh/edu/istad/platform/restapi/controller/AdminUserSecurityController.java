package kh.edu.istad.platform.restapi.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import kh.edu.istad.platform.iam.application.dto.AccountSessionResult;
import kh.edu.istad.platform.iam.application.dto.LoginHistoryResult;
import kh.edu.istad.platform.iam.application.usecase.GetUserUseCase;
import kh.edu.istad.platform.iam.application.usecase.ListAccountSessionsUseCase;
import kh.edu.istad.platform.iam.application.usecase.ListLoginHistoryUseCase;
import kh.edu.istad.platform.iam.application.usecase.RevokeAccountSessionsUseCase;
import kh.edu.istad.platform.restapi.dto.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users/{uuid}")
@RequiredArgsConstructor
@Validated
public class AdminUserSecurityController {

    private final GetUserUseCase getUser;
    private final ListAccountSessionsUseCase listSessions;
    private final ListLoginHistoryUseCase loginHistory;
    private final RevokeAccountSessionsUseCase revokeSessions;

    @GetMapping("/sessions")
    @PreAuthorize("hasAuthority('user:read')")
    public List<AccountSessionResult> sessions(@PathVariable UUID uuid) {
        getUser.execute(uuid);
        return listSessions.executeForUser(uuid);
    }

    @GetMapping("/login-history")
    @PreAuthorize("hasAuthority('user:read')")
    public PageResponse<LoginHistoryResult> loginHistory(
            @PathVariable UUID uuid,
            @RequestParam(defaultValue = "all") @Pattern(regexp = "all|success|failed") String result,
            @RequestParam(required = false) @Min(1) @Max(3650) Integer days,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size
    ) {
        getUser.execute(uuid);
        return PageResponse.from(loginHistory.execute(uuid, result, days, page, size), entry -> entry);
    }

    @DeleteMapping("/sessions/{sessionId}")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<Void> revokeSession(@PathVariable UUID uuid, @PathVariable UUID sessionId) {
        revokeSessions.revokeOne(uuid, sessionId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/sessions")
    @PreAuthorize("hasAuthority('user:update')")
    public AccountSecurityController.TerminationResponse revokeAllSessions(@PathVariable UUID uuid) {
        getUser.execute(uuid);
        return new AccountSecurityController.TerminationResponse(revokeSessions.revokeAll(uuid));
    }
}
