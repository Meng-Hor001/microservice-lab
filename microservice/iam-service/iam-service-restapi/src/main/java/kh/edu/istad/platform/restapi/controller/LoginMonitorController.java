package kh.edu.istad.platform.restapi.controller;

import jakarta.validation.Valid;
import kh.edu.istad.platform.iam.application.dto.SuspiciousLoginResult;
import kh.edu.istad.platform.iam.application.usecase.ListSuspiciousLoginsUseCase;
import kh.edu.istad.platform.restapi.dto.request.SuspiciousLoginSearchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/login-history")
@RequiredArgsConstructor
public class LoginMonitorController {
    private final ListSuspiciousLoginsUseCase suspiciousLogins;

    @GetMapping("/suspicious")
    @PreAuthorize("hasAuthority('user:read')")
    public List<SuspiciousLoginResult> suspicious(@Valid SuspiciousLoginSearchRequest request) {
        return suspiciousLogins.execute(request.daysOrDefault(), request.thresholdOrDefault());
    }
}
