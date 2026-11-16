package kh.edu.istad.platform.iam.application.port.out;

import kh.edu.istad.platform.iam.application.dto.AuditLogFilterQuery;
import kh.edu.istad.platform.iam.application.dto.AuditLogResult;
import kh.edu.istad.platform.iam.application.dto.PageResult;
import kh.edu.istad.platform.iam.application.dto.RecordAuditLogCommand;

public interface AuditLogRepository {
    void save(RecordAuditLogCommand command);

    PageResult<AuditLogResult> findAll(AuditLogFilterQuery query);
}
