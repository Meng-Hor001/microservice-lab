package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.dto.AuditLogFilterQuery;
import kh.edu.istad.platform.iam.application.dto.AuditLogResult;
import kh.edu.istad.platform.iam.application.dto.PageResult;
import kh.edu.istad.platform.iam.application.port.out.AuditLogRepository;

import java.util.Objects;

public final class ListAuditLogsUseCase {
    private final AuditLogRepository auditLogs;

    public ListAuditLogsUseCase(AuditLogRepository auditLogs) {
        this.auditLogs = Objects.requireNonNull(auditLogs);
    }

    public PageResult<AuditLogResult> execute(AuditLogFilterQuery query) {
        return auditLogs.findAll(Objects.requireNonNull(query));
    }
}
