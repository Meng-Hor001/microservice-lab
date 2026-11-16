package kh.edu.istad.platform.restapi.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import kh.edu.istad.platform.iam.application.dto.AuditLogFilterQuery;
import kh.edu.istad.platform.iam.application.dto.AuditLogResult;
import kh.edu.istad.platform.iam.application.usecase.ListAuditLogsUseCase;
import kh.edu.istad.platform.restapi.dto.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@Validated
public class AuditLogController {

    private final ListAuditLogsUseCase listAuditLogs;

    @GetMapping
    @PreAuthorize("hasAuthority('audit:read')")
    public PageResponse<AuditLogResult> list(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String actor,
            @RequestParam(required = false)
            @Pattern(regexp = "USER|ROLE|PERMISSION|CLIENT|SETTINGS|AUTH") String targetType,
            @RequestParam(required = false) String targetId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        var query = new AuditLogFilterQuery(action, actor, targetType, targetId, from, to, page, size);
        return PageResponse.from(listAuditLogs.execute(query), entry -> entry);
    }
}
