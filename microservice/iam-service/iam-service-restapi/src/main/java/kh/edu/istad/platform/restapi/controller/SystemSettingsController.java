package kh.edu.istad.platform.restapi.controller;

import jakarta.validation.Valid;
import kh.edu.istad.platform.iam.application.dto.SystemSettingsResult;
import kh.edu.istad.platform.iam.application.usecase.ManageSystemSettingsUseCase;
import kh.edu.istad.platform.restapi.audit.Audited;
import kh.edu.istad.platform.restapi.dto.request.UpdateSystemSettingsRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SystemSettingsController {
    private final ManageSystemSettingsUseCase settings;

    @GetMapping
    @PreAuthorize("hasAuthority('settings:read')")
    public SystemSettingsResult get() {
        return settings.get();
    }

    @PutMapping
    @PreAuthorize("hasAuthority('settings:update')")
    @Audited(action = "SETTINGS_UPDATED", target = "SETTINGS")
    public SystemSettingsResult update(@Valid @RequestBody UpdateSystemSettingsRequest request) {
        return settings.update(request.toCommand());
    }
}
